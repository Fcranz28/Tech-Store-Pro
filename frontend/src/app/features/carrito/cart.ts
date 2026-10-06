import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { StoreApi, money } from '../../core/store-api';
import { ProductVisual } from '../../shared/product-visual';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-cart',
  imports: [RouterLink, ProductVisual],
  template: `
    <section class="container inner-page">
      <div class="page-heading"><div><h1>Tu carrito</h1><p>Todo lo que elegiste, listo para el siguiente paso.</p></div><a routerLink="/productos" class="text-link">Seguir comprando →</a></div>
      @if (api.transferError()) { <p role="alert" class="cart-transfer-alert">{{ api.transferError() }} <button type="button" (click)="retryTransfer()">Reintentar</button></p> }
      @if (loading()) { <p class="feedback">Cargando carrito…</p> }
      @else if (error()) { <div class="feedback feedback-error" role="alert">{{ error() }} <button (click)="load()">Reintentar</button></div> }
      @else if (!api.cart().items.length) {
        <div class="empty-state"><h2>Tu carrito está esperando.</h2><p>Explora el catálogo y encuentra algo para ti.</p><a routerLink="/productos" class="button button-primary">Ver productos</a></div>
      } @else {
        <div class="checkout-grid">
          <div class="cart-lines">
            @for (item of api.cart().items; track item.id) {
              <article class="cart-line">
                <div class="cart-image"><app-product-visual [product]="asProduct(item)" /></div>
                <div class="cart-details"><span class="product-category">{{ item.categoria }}</span><h2>{{ item.nombre }}</h2><p>{{ money(item.precio) }} / unidad</p>
                  <button type="button" class="remove-link" (click)="remove(item.id)" [disabled]="busy()">Eliminar</button>
                </div>
                <div class="cart-controls"><label [for]="'qty-' + item.id">Cantidad</label><input type="number" min="1" [max]="item.stock" [id]="'qty-' + item.id" [value]="item.cantidad" (change)="change(item.id, item.stock, $event)" [disabled]="busy()"><strong>{{ money(item.subtotal) }}</strong></div>
              </article>
            }
          </div>
          <aside class="order-summary"><h2>Resumen</h2><div class="summary-row"><span>{{ api.cart().items.length }} {{ api.cart().items.length === 1 ? 'artículo' : 'artículos' }}</span><strong>{{ money(api.cart().total) }}</strong></div>
            <div class="summary-total"><span>Total</span><strong>{{ money(api.cart().total) }}</strong></div>
            <p>El pedido se confirma con el stock y precio actuales. No incluye procesamiento de pago.</p>
            @if (actionError()) { <p role="alert" class="form-error">{{ actionError() }}</p> }
            <button class="button button-primary full-width" [disabled]="busy() || !!api.transferError()" (click)="confirm()">{{ busy() ? 'Procesando…' : auth.token() ? 'Confirmar pedido' : 'Continuar para confirmar' }}</button>
          </aside>
        </div>
      }
    </section>
  `
})
export class CartPage implements OnInit {
  readonly api = inject(StoreApi);
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly actionError = signal('');
  readonly busy = signal(false);
  readonly money = money;
  ngOnInit() { this.load(); }
  load() {
    this.loading.set(true); this.error.set('');
    this.api.restoreAfterLogin().subscribe({
      next: () => this.loading.set(false),
      error: () => { if (!this.api.transferError()) this.error.set('No pudimos cargar tu carrito.'); this.loading.set(false); }
    });
  }
  asProduct(item: { productoId: number; nombre: string; categoria: string; imagenUrl: string | null; precio: number; stock: number }) {
    return { id: item.productoId, nombre: item.nombre, categoria: item.categoria, imagenUrl: item.imagenUrl,
      descripcion: '', precio: item.precio, stock: item.stock, destacado: false, activo: true };
  }
  change(id: number, stock: number, event: Event) {
    const quantity = Number((event.target as HTMLInputElement).value);
    if (!Number.isInteger(quantity) || quantity < 1 || quantity > stock) {
      this.actionError.set(`Elige una cantidad entre 1 y ${stock}.`);
      this.load();
      return;
    }
    this.busy.set(true); this.actionError.set('');
    this.api.changeQuantity(id, quantity).subscribe({
      next: () => this.busy.set(false),
      error: () => { this.actionError.set('No se pudo cambiar la cantidad. Actualiza el carrito.'); this.busy.set(false); this.load(); }
    });
  }
  remove(id: number) {
    this.busy.set(true); this.actionError.set('');
    this.api.removeItem(id).subscribe({
      next: () => { this.busy.set(false); this.load(); },
      error: () => { this.actionError.set('No se pudo eliminar el artículo.'); this.busy.set(false); }
    });
  }
  confirm() {
    if (this.api.transferError()) return;
    if (!this.auth.token()) { void this.router.navigate(['/login'], { queryParams: { next: '/carrito' } }); return; }
    this.busy.set(true); this.actionError.set('');
    this.api.confirmOrder().subscribe({
      next: order => void this.router.navigate(['/pedidos'], { queryParams: { confirmado: order.id } }),
      error: (e: HttpErrorResponse) => { this.actionError.set(e.status === 409 ? 'El stock de algún producto cambió. Revisa tu carrito.' : 'No se pudo confirmar el pedido.'); this.busy.set(false); this.load(); }
    });
  }
  retryTransfer() {
    this.busy.set(true);
    this.api.restoreAfterLogin().subscribe({ next: () => this.busy.set(false),
      error: () => { this.actionError.set('No se pudo recuperar tu selección. Inténtalo de nuevo.'); this.busy.set(false); } });
  }
}
