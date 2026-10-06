import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Order, StoreApi, money, orderStatusLabel } from '../../core/store-api';
import { OrderStepper } from '../../shared/order-stepper';

@Component({
  selector: 'app-orders',
  imports: [RouterLink, OrderStepper],
  template: `
    <section class="container inner-page">
      <div class="page-heading"><div><h1>Mis pedidos</h1><p>Consulta el avance de tus compras en un solo lugar.</p></div><div class="orders-heading-actions"><button class="button button-outline" type="button" [disabled]="loading()" (click)="load()">{{ loading() ? 'Actualizando…' : 'Actualizar estados' }}</button><a routerLink="/productos" class="text-link">Explorar tienda →</a></div></div>
      @if (confirmed()) { <div class="success-panel" role="status"><strong>Pedido #{{ confirmed() }} confirmado</strong><span>Tu selección quedó registrada correctamente.</span></div> }
      @if (loading()) { <p class="feedback">Cargando pedidos…</p> }
      @else if (error()) { <div class="feedback feedback-error" role="alert">{{ error() }} <button (click)="load()">Reintentar</button></div> }
      @else if (!orders().length) { <div class="empty-state"><h2>Aún no tienes pedidos.</h2><p>Cuando confirmes tu primera compra, aparecerá aquí.</p><a routerLink="/productos" class="button button-primary">Ver productos</a></div> }
      @else {
        <div class="orders-list">@for (order of orders(); track order.id) {
          <article class="order-card"><div class="order-card-head"><div><span class="product-category">Pedido #{{ order.id }}</span><h2>{{ date(order.createdAt) }}</h2></div><span class="status-pill">{{ statusLabel(order.estado) }}</span></div>
            <app-order-stepper [status]="order.estado" />
            <ul>@for (item of order.items; track item.productoId) { <li><span>{{ item.nombre }} <small>× {{ item.cantidad }}</small></span><strong>{{ money(item.subtotal) }}</strong></li> }</ul>
            <div class="order-card-total"><span>Total</span><strong>{{ money(order.total) }}</strong></div>
          </article>
        }</div>
      }
    </section>
  `
})
export class OrdersPage implements OnInit {
  private readonly api = inject(StoreApi);
  private readonly route = inject(ActivatedRoute);
  readonly orders = signal<Order[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly confirmed = signal(this.route.snapshot.queryParamMap.get('confirmado'));
  readonly money = money;
  readonly statusLabel = orderStatusLabel;
  readonly date = (value: string) => new Intl.DateTimeFormat('es-PE', { dateStyle: 'long', timeStyle: 'short' }).format(new Date(value));
  ngOnInit() { this.load(); }
  load() {
    this.loading.set(true); this.error.set('');
    this.api.myOrders().subscribe({
      next: orders => { this.orders.set(orders); this.loading.set(false); },
      error: () => { this.error.set('No pudimos cargar tus pedidos.'); this.loading.set(false); }
    });
  }
}
