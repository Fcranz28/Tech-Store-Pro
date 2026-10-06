import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Product, StoreApi, money } from '../../core/store-api';
import { ProductVisual } from '../../shared/product-visual';

@Component({
  selector: 'app-home',
  imports: [RouterLink, ProductVisual],
  template: `
    <section class="hero" aria-labelledby="hero-title">
      <div class="hero-image"></div>
      <div class="hero-content container">
        <h1 id="hero-title">Tecnología para vivir<br><em>a tu manera.</em></h1>
        <p>Audio y periféricos que se adaptan a tu música, tus partidas y tus ideas. Encuentra el equipo para lo que viene.</p>
        <a class="hero-cta" routerLink="/productos"><span>Explorar productos</span><span class="hero-cta-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M5 19 19 5M8 5h11v11"/></svg></span></a>
      </div>
    </section>
    <section class="brand-strip" aria-label="Marcas conocidas de tecnología">
      <div class="container">
        <div class="brand-strip-heading"><span></span><p>Marcas conocidas del mundo tech</p><span></span></div>
        <div class="brand-marquee">
          <div class="brand-track">
            <ul class="brand-list" aria-label="Marcas de referencia">
              <li>Logitech</li><li>SONY</li><li>JBL</li><li>Razer</li><li>HyperX</li>
            </ul>
            <ul class="brand-list" aria-hidden="true">
              <li>Logitech</li><li>SONY</li><li>JBL</li><li>Razer</li><li>HyperX</li>
            </ul>
          </div>
        </div>
      </div>
    </section>
    <section class="home-showcase" aria-labelledby="home-showcase-title">
      <div class="container">
        <div class="home-showcase-heading">
          <div><h2 id="home-showcase-title">Encuentra lo que va contigo.</h2><p>Una selección para escuchar, crear y jugar a tu manera.</p></div>
          <a routerLink="/productos" class="home-all-link">Ver todo el catálogo <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><path d="M5 19 19 5M8 5h11v11"/></svg></a>
        </div>
        @if (loading()) {
          <p class="home-showcase-state" role="status">Cargando productos…</p>
        } @else if (error()) {
          <div class="home-showcase-state" role="alert">{{ error() }} <button type="button" (click)="load()">Reintentar</button></div>
        } @else if (products().length) {
          <div class="home-carousel-controls">
            <span>{{ activeIndex() + 1 }} / {{ products().length }} · Explora la selección</span>
            <div class="home-carousel-buttons">
              <button type="button" aria-label="Ver producto anterior" [disabled]="activeIndex() === 0" (click)="select(activeIndex() - 1)"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><path d="M20 12H4m6-6-6 6 6 6"/></svg></button>
              <button type="button" aria-label="Ver producto siguiente" [disabled]="activeIndex() === products().length - 1" (click)="select(activeIndex() + 1)"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><path d="M4 12h16m-6-6 6 6-6 6"/></svg></button>
            </div>
          </div>
          <div class="home-carousel" role="region" aria-roledescription="carrusel" aria-label="Productos destacados" tabindex="0" (keydown.arrowleft)="onArrow($event, -1)" (keydown.arrowright)="onArrow($event, 1)">
            @for (product of products(); track product.id; let i = $index) {
              @if (distance(i) <= 3) {
                <article class="home-product-card" [class.is-active]="i === activeIndex()" [style.--position]="i - activeIndex()" [style.--distance]="distance(i)" [style.z-index]="products().length - distance(i)">
                <div class="home-product-image"><app-product-visual [product]="product" />@if (product.destacado) { <span class="home-product-tag">Nuestra selección</span> }
                  <div class="home-product-details"><span>{{ categoryLabel(product.categoria) }}</span><h3>{{ product.nombre }}</h3><strong>{{ money(product.precio) }}</strong></div>
                </div>
                @if (i === activeIndex()) {
                  <a class="home-card-hit" [routerLink]="['/productos', product.id]" [attr.aria-label]="'Ver detalles de ' + product.nombre"></a>
                } @else {
                  <button type="button" class="home-card-hit" (click)="select(i)" [attr.aria-label]="'Seleccionar ' + product.nombre"></button>
                }
                </article>
              }
            }
          </div>
          @if (activeProduct(); as item) {
            <div class="home-carousel-feature" aria-live="polite">
              <h3>{{ item.nombre }}</h3>
              <span>{{ categoryLabel(item.categoria) }}</span>
              <p>{{ item.descripcion }}</p>
              <div class="home-carousel-facts"><div><span>Precio</span><strong>{{ money(item.precio) }}</strong></div><div><span>Disponibilidad</span><strong>{{ item.stock > 0 ? item.stock + ' disponibles' : 'Sin stock' }}</strong></div></div>
              <a [routerLink]="['/productos', item.id]">Ver producto <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" aria-hidden="true"><path d="M5 19 19 5M8 5h11v11"/></svg></a>
            </div>
          }
        } @else {
          <div class="home-showcase-state">Aún no hay productos disponibles. <a routerLink="/productos">Visita el catálogo</a></div>
        }
      </div>
    </section>
    <section class="home-next container" aria-labelledby="home-next-title">
      <span class="home-next-label">Sigue explorando</span>
      <h2 id="home-next-title">El siguiente favorito<br>te espera.</h2>
      <p>Explora el catálogo completo y encuentra el accesorio que encaja con tu día.</p>
      <div class="home-next-actions">
        <a class="home-next-cart" routerLink="/carrito">Ver carrito <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 5h2l2 10h10l2-7H7"/><circle cx="9" cy="19" r="1"/><circle cx="18" cy="19" r="1"/></svg></a>
        <a class="home-next-catalog" routerLink="/productos">Explorar productos <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" aria-hidden="true"><path d="M5 12h14m-6-6 6 6-6 6"/></svg></a>
      </div>
    </section>
  `
})
export class Home implements OnInit {
  private readonly api = inject(StoreApi);
  readonly products = signal<Product[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly activeIndex = signal(0);
  readonly activeProduct = computed(() => this.products()[this.activeIndex()] ?? null);
  readonly money = money;

  ngOnInit() { this.load(); }
  load() {
    this.loading.set(true);
    this.error.set('');
    this.api.products().subscribe({
      next: products => {
        this.products.set([...products].sort((a, b) => Number(b.destacado) - Number(a.destacado)));
        this.activeIndex.set(Math.min(2, Math.floor((products.length - 1) / 2)));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No pudimos cargar la selección. Comprueba la conexión e inténtalo de nuevo.');
        this.loading.set(false);
      }
    });
  }
  categoryLabel(category: string) {
    return ({ AUDIFONOS: 'Audífonos', TECLADOS: 'Teclados', MOUSE: 'Mouse', AUDIO: 'Audio', ACCESORIOS: 'Accesorios' } as Record<string, string>)[category] ?? category;
  }
  distance(index: number) { return Math.abs(index - this.activeIndex()); }
  select(index: number) {
    if (index >= 0 && index < this.products().length) this.activeIndex.set(index);
  }
  onArrow(event: Event, direction: number) {
    event.preventDefault();
    this.select(this.activeIndex() + direction);
  }
}
