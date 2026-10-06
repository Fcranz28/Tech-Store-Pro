import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, of, switchMap, tap } from 'rxjs';
import { CatalogResponse, Product, StoreApi, money } from '../../core/store-api';
import { ProductVisual } from '../../shared/product-visual';

@Component({
  selector: 'app-catalog',
  imports: [RouterLink, ProductVisual, FormsModule],
  template: `
    <section class="shop-catalog"><div class="container shop-layout">
      <aside class="shop-sidebar" aria-label="Categorías de productos">
        <h2>Explorar productos</h2><p>Encuentra tu próximo favorito.</p>
        <nav><button type="button" [class.active]="!category()" [attr.aria-pressed]="!category()" (click)="selectCategory('')"><span>Todos los productos</span><small>{{ catalog()?.totalCatalogo ?? '—' }}</small></button>
          @for (item of catalog()?.categorias ?? []; track item.value) { <button type="button" [class.active]="category() === item.value" [attr.aria-pressed]="category() === item.value" (click)="selectCategory(item.value)"><span>{{ item.label }}</span><small>{{ item.count }}</small></button> }
        </nav>
        <div class="shop-sidebar-note"><span aria-hidden="true">↗</span><h3>A tu manera.</h3><p>Audio y periféricos para trabajar, jugar y conectar.</p><a routerLink="/">Volver al inicio →</a></div>
      </aside>
      <main class="shop-main">
        <nav class="shop-breadcrumb" aria-label="Ruta de navegación"><a routerLink="/">Inicio</a><span aria-hidden="true">›</span>@if (category()) { <button type="button" (click)="clear()">Productos</button><span aria-hidden="true">›</span> }<span aria-current="page">{{ category() ? title() : 'Productos' }}</span></nav>
        <header class="shop-heading"><span>ELIGE TU EQUIPO</span><h1>{{ title() }}</h1><p>Diseñado para acompañar lo que haces cada día.</p></header>
        <form class="shop-filters" (ngSubmit)="apply()">
          <label class="shop-search"><span>Buscar producto</span><div><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" aria-hidden="true"><circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/></svg><input name="q" [(ngModel)]="search" maxlength="100" placeholder="Nombre o descripción"></div></label>
          <fieldset class="shop-price"><legend>Precio (S/)</legend><div><input name="precioMin" type="number" min="0" step="0.01" [(ngModel)]="minPrice" placeholder="Desde" aria-label="Precio mínimo"><span aria-hidden="true">–</span><input name="precioMax" type="number" min="0" step="0.01" [(ngModel)]="maxPrice" placeholder="Hasta" aria-label="Precio máximo"></div></fieldset>
          <label><span>Disponibilidad</span><select name="disponible" [(ngModel)]="availability"><option value="">Todos</option><option value="true">Con stock</option><option value="false">Agotados</option></select></label>
          <label><span>Selección</span><select name="destacado" [(ngModel)]="featured"><option value="">Todos</option><option value="true">Destacados</option></select></label>
          <button class="shop-apply" type="submit">Filtrar <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><path d="M4 7h16M7 12h10M10 17h4"/></svg></button>
        </form>
        <div class="shop-results-toolbar"><div><span>{{ loading() ? 'Buscando productos…' : error() ? 'Catálogo no disponible' : catalog()?.total + ' productos encontrados' }}</span>@if (hasFilters()) { <button type="button" (click)="clear()">Limpiar filtros ×</button> }</div><label>Ordenar por<select aria-label="Ordenar productos" [ngModel]="sort()" (ngModelChange)="changeSort($event)"><option value="recientes">Más recientes</option><option value="precio-asc">Menor precio</option><option value="precio-desc">Mayor precio</option><option value="nombre">Nombre A–Z</option></select></label></div>
        @if (loading()) { <div class="shop-state" role="status">Cargando el catálogo…</div> }
        @else if (error()) { <div class="shop-state" role="alert"><h2>No pudimos mostrar los productos</h2><p>{{ error() }}</p><button type="button" (click)="retry()">Reintentar</button><button type="button" (click)="clear()">Restablecer filtros</button></div> }
        @else if (!catalog()?.productos?.length) { <div class="shop-state"><h2>No encontramos productos</h2><p>Prueba con otra búsqueda, categoría o rango de precio.</p><button type="button" (click)="clear()">Ver todos los productos</button></div> }
        @else {
          <div class="shop-product-grid">@for (product of catalog()!.productos; track product.id) {
            <article class="shop-product-card" [class.featured]="product.destacado">
              <a class="shop-card-link" [routerLink]="['/productos', product.id]" [attr.aria-label]="'Ver producto: ' + product.nombre"></a>
              <div class="shop-product-image"><app-product-visual [product]="product" />@if (product.destacado) { <span class="shop-product-badge">Destacado</span> }@if (!product.stock) { <span class="shop-stock-badge">Agotado</span> }</div>
              <div class="shop-product-info"><span class="shop-product-category">{{ label(product.categoria) }}</span><h2>{{ product.nombre }}</h2><strong class="shop-product-price">{{ money(product.precio) }}</strong><span class="shop-product-stock" [class.empty]="!product.stock"><i aria-hidden="true"></i>{{ product.stock ? product.stock + ' disponibles' : 'Sin stock' }}</span>
                <div class="shop-product-actions"><a [routerLink]="['/productos', product.id]">Ver producto <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><path d="M5 12h14m-6-6 6 6-6 6"/></svg></a><button type="button" [disabled]="!product.stock || busyIds().includes(product.id)" (click)="add(product)" [attr.aria-label]="'Añadir ' + product.nombre + ' al carrito'"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 4h2l2 11h12l2-8H6"/><circle cx="9" cy="20" r="1"/><circle cx="18" cy="20" r="1"/></svg>{{ busyIds().includes(product.id) ? 'Añadiendo…' : product.stock ? 'Añadir' : 'Agotado' }}</button></div>
              </div>
            </article>
          }</div>
          @if (catalog()!.totalPaginas > 1) { <nav class="shop-pagination" aria-label="Páginas del catálogo"><button type="button" [disabled]="catalog()!.pagina === 0" (click)="page(catalog()!.pagina - 1)">← Anterior</button><span>Página {{ catalog()!.pagina + 1 }} de {{ catalog()!.totalPaginas }}</span><button type="button" [disabled]="catalog()!.pagina + 1 >= catalog()!.totalPaginas" (click)="page(catalog()!.pagina + 1)">Siguiente →</button></nav> }
        }
      </main>
    </div>@if (notice()) { <p class="catalog-notice" role="status">{{ notice() }} <a routerLink="/carrito">Ver carrito</a><button type="button" (click)="notice.set('')" aria-label="Cerrar aviso">×</button></p> }</section>
  `
})
export class Catalog implements OnInit {
  private readonly api = inject(StoreApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly catalog = signal<CatalogResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly notice = signal('');
  readonly busyIds = signal<number[]>([]);
  readonly category = signal('');
  readonly sort = signal('recientes');
  readonly hasFilters = signal(false);
  readonly title = computed(() => this.category() ? this.label(this.category()) : 'Todos los productos');
  readonly money = money;
  search = ''; minPrice: number | null = null; maxPrice: number | null = null; availability = ''; featured = '';
  private params: Record<string, string> = {};
  ngOnInit() {
    this.route.queryParamMap.pipe(tap(params => {
      this.params = {};
      for (const key of ['categoria', 'q', 'precioMin', 'precioMax', 'disponible', 'destacado', 'orden', 'pagina']) {
        const value = params.get(key); if (value) this.params[key] = value;
      }
      this.category.set(params.get('categoria') || ''); this.sort.set(params.get('orden') || 'recientes');
      this.search = params.get('q') || ''; this.availability = params.get('disponible') || ''; this.featured = params.get('destacado') || '';
      this.minPrice = params.has('precioMin') ? Number(params.get('precioMin')) : null;
      this.maxPrice = params.has('precioMax') ? Number(params.get('precioMax')) : null;
      this.hasFilters.set(['categoria', 'q', 'precioMin', 'precioMax', 'disponible', 'destacado'].some(key => !!params.get(key)));
      this.loading.set(true); this.error.set('');
    }), switchMap(() => this.request()), takeUntilDestroyed(this.destroyRef)).subscribe();
  }
  private request() {
    return this.api.catalog(this.params).pipe(tap(response => { this.catalog.set(response); this.loading.set(false); }), catchError((error: HttpErrorResponse) => {
      this.error.set(error.status === 400 ? 'Revisa los filtros: usa precios positivos y un mínimo menor o igual al máximo.' : 'Comprueba la conexión con el servidor e inténtalo de nuevo.');
      this.loading.set(false); return of(null);
    }));
  }
  retry() { this.loading.set(true); this.error.set(''); this.request().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(); }
  label(category: string) { return this.catalog()?.categorias.find(item => item.value === category)?.label ?? category; }
  private navigate(params: Record<string, string>) { void this.router.navigate(['/productos'], { queryParams: params }); }
  selectCategory(value: string) { const params = { ...this.params }; delete params['pagina']; if (value) params['categoria'] = value; else delete params['categoria']; this.navigate(params); }
  apply() {
    const params = { ...this.params }; delete params['pagina'];
    for (const [key, value] of Object.entries({ q: this.search.trim(), precioMin: this.minPrice, precioMax: this.maxPrice, disponible: this.availability, destacado: this.featured })) {
      if (value !== null && value !== '') params[key] = String(value); else delete params[key];
    }
    this.navigate(params);
  }
  changeSort(value: string) { const params: Record<string, string> = { ...this.params, orden: value }; delete params['pagina']; this.navigate(params); }
  page(value: number) { this.navigate({ ...this.params, pagina: String(value) }); window.scrollTo({ top: 0, behavior: 'smooth' }); }
  clear() { this.navigate({}); if (!Object.keys(this.params).length) this.retry(); }
  add(product: Product) {
    if (this.busyIds().includes(product.id)) return;
    this.busyIds.update(ids => [...ids, product.id]); this.notice.set('');
    this.api.addToCart(product.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.notice.set(product.nombre + ' se añadió al carrito.'); this.busyIds.update(ids => ids.filter(id => id !== product.id)); },
      error: (error: HttpErrorResponse) => { this.notice.set(error.status === 409 ? 'El stock cambió. Actualiza el catálogo e inténtalo de nuevo.' : 'No se pudo añadir el producto.'); this.busyIds.update(ids => ids.filter(id => id !== product.id)); if (error.status === 409) this.retry(); }
    });
  }
}
