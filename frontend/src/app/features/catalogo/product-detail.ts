import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Product, StoreApi, money } from '../../core/store-api';
import { ProductVisual } from '../../shared/product-visual';

@Component({
  selector: 'app-product-detail',
  imports: [RouterLink, ProductVisual],
  template: `
    <main class="detail-surface">
      <div class="container detail-container">
        <nav class="detail-breadcrumb" aria-label="Ruta de navegación"><a routerLink="/">Inicio</a><span aria-hidden="true">/</span><a routerLink="/productos">Productos</a>@if (product(); as item) { <span aria-hidden="true">/</span><span aria-current="page">{{ item.nombre }}</span> }</nav>
        @if (loading()) {
          <p class="detail-feedback" role="status">Cargando producto…</p>
        } @else if (error()) {
          <div class="detail-feedback" role="alert"><p>{{ error() }}</p><a routerLink="/productos">Volver al catálogo</a> <button type="button" (click)="load()">Reintentar</button></div>
        } @else if (product(); as item) {
          <div class="detail-layout">
            <div class="detail-gallery">
              <div class="detail-media"><app-product-visual [product]="item" /></div>
              <div class="detail-media-caption"><span>Imagen del producto</span><a routerLink="/productos">Explorar catálogo <span aria-hidden="true">→</span></a></div>
            </div>
            <div class="detail-copy">
              <div class="detail-tools"><button type="button" (click)="share()" aria-label="Copiar enlace del producto"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="m8.6 10.5 6.8-4m-6.8 7 6.8 4"/></svg></button></div>
              @if (shareNotice()) { <p class="detail-share-notice" role="status">{{ shareNotice() }}</p> }
              <h1>{{ item.nombre }}</h1>
              <div class="detail-purchase">
                <strong class="detail-price">{{ money(item.precio) }}</strong>
                <div class="detail-actions">
                  <button class="button button-primary detail-add" type="button" [disabled]="item.stock === 0 || busy()" (click)="add(item)"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 4h2l2 11h11l2-7H6"/><circle cx="9" cy="20" r="1"/><circle cx="18" cy="20" r="1"/></svg>{{ item.stock === 0 ? 'Agotado' : busy() ? 'Añadiendo…' : 'Añadir al carrito' }}</button>
                  <a class="detail-cart" routerLink="/carrito">Ver carrito</a>
                </div>
                @if (notice()) { <p class="detail-notice" role="status">{{ notice() }} @if (added()) { <a routerLink="/carrito">Ver carrito</a> }</p> }
              </div>
              <div class="detail-tags"><span>{{ categoryLabel(item.categoria) }}</span><span class="detail-stock" [class.out-of-stock]="item.stock === 0">{{ item.stock > 0 ? item.stock + ' disponibles' : 'Sin stock' }}</span>@if (item.destacado) { <span>Nuestra selección</span> }</div>
              <p class="detail-description">{{ item.descripcion }}</p>
              <div class="detail-store"><div class="detail-store-brand"><span class="brand-symbol" aria-hidden="true"></span><div><strong>TechStore Pro</strong><span>Audio y periféricos</span></div></div><a routerLink="/productos">Ver catálogo <span aria-hidden="true">→</span></a></div>
            </div>
          </div>
        }
      </div>
    </main>
  `
})
export class ProductDetail implements OnInit {
  private readonly api = inject(StoreApi);
  private readonly route = inject(ActivatedRoute);
  readonly product = signal<Product | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly notice = signal('');
  readonly busy = signal(false);
  readonly added = signal(false);
  readonly shareNotice = signal('');
  readonly money = money;

  ngOnInit() { this.load(); }
  load() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isSafeInteger(id) || id <= 0) {
      this.loading.set(false);
      this.error.set('Este producto no existe.');
      return;
    }
    this.loading.set(true);
    this.error.set('');
    this.api.product(id).subscribe({
      next: product => { this.product.set(product); this.loading.set(false); },
      error: (e: HttpErrorResponse) => {
        this.error.set(e.status === 404 ? 'Este producto ya no está disponible.' : 'No pudimos cargar el producto. Comprueba la conexión con el servidor.');
        this.loading.set(false);
      }
    });
  }
  categoryLabel(category: string) {
    return ({ AUDIFONOS: 'Audífonos', TECLADOS: 'Teclados', MOUSE: 'Mouse', AUDIO: 'Audio', ACCESORIOS: 'Accesorios' } as Record<string, string>)[category] ?? category;
  }
  async share() {
    try { await navigator.clipboard.writeText(window.location.href); this.shareNotice.set('Enlace del producto copiado.'); }
    catch { this.shareNotice.set('No se pudo copiar el enlace. Puedes copiarlo desde la barra de direcciones.'); }
  }
  add(product: Product) {
    this.busy.set(true);
    this.notice.set('');
    this.added.set(false);
    this.api.addToCart(product.id).subscribe({
      next: () => { this.notice.set('Producto añadido al carrito.'); this.added.set(true); this.busy.set(false); },
      error: (e: HttpErrorResponse) => { this.notice.set(e.status === 409 ? 'El stock cambió. Actualiza la página e inténtalo de nuevo.' : 'No se pudo añadir el producto. Inténtalo de nuevo.'); this.busy.set(false); }
    });
  }
}
