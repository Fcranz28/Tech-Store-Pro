import { Component, HostListener, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { AuthService } from './core/auth.service';
import { StoreApi } from './core/store-api';
import { ProductVisual } from './shared/product-visual';
import { money } from './core/store-api';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ProductVisual],
  template: `
    <div class="site-shell">
      @if (!adminPage()) {
      <header class="site-header">
        <div class="container header-inner">
          <a routerLink="/" class="brand" aria-label="TechStore Pro, inicio"><span class="brand-symbol" aria-hidden="true"></span><span>TechStore <b>Pro</b></span></a>
          <nav class="main-nav" aria-label="Navegación principal">
            <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Inicio</a>
            <a routerLink="/productos" routerLinkActive="active">Productos</a>
            @if (auth.token()) { <a routerLink="/pedidos" routerLinkActive="active">Mis pedidos</a> }
            @if (auth.user()?.rol === 'ADMIN') { <a routerLink="/admin" routerLinkActive="active">Administración</a> }
          </nav>
          <div class="header-actions">
            @if (auth.token()) {
              <a routerLink="/perfil" class="icon-link" aria-label="Mi cuenta" title="Mi cuenta"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><circle cx="12" cy="8" r="3.5"/><path d="M5 20c.5-3.7 3-5.5 7-5.5s6.5 1.8 7 5.5"/></svg></a>
            } @else { <a routerLink="/login" class="header-login">Ingresar</a><a routerLink="/registro" class="header-register">Crear cuenta</a> }
            <button type="button" class="icon-link cart-link" aria-label="Abrir carrito" (click)="api.sidebarOpen.set(true)"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><path d="M4 7h16l-1.3 13H5.3L4 7ZM9 9V6a3 3 0 0 1 6 0v3"/></svg>@if (api.cart().items.length) { <span class="cart-count">{{ api.cart().items.length }}</span> }</button>
          </div>
        </div>
      </header>
      }
      @if (api.sidebarOpen()) {
        <div class="cart-drawer-backdrop" (click)="api.sidebarOpen.set(false)"></div>
        <aside class="cart-drawer" aria-label="Carrito de compras">
          <div class="cart-drawer-head"><div><span>Tu selección</span><h2>Carrito</h2></div><button type="button" aria-label="Cerrar carrito" (click)="api.sidebarOpen.set(false)">×</button></div>
          @if (api.cart().items.length) {
            <div class="cart-drawer-items">
              @for (item of api.cart().items; track item.id) {
                <article class="cart-drawer-item"><div class="cart-drawer-image"><app-product-visual [product]="asProduct(item)" /></div>
                  <div><strong>{{ item.nombre }}</strong><small>{{ item.cantidad }} × {{ money(item.precio) }}</small><button type="button" [disabled]="sidebarBusy()" (click)="removeFromSidebar(item.id)">Quitar</button></div>
                  <b>{{ money(item.subtotal) }}</b></article>
              }
            </div>
            <div class="cart-drawer-footer"><div><span>Subtotal</span><strong>{{ money(api.cart().total) }}</strong></div>
              @if (sidebarError()) { <p role="alert">{{ sidebarError() }}</p> }
              <a routerLink="/carrito" class="button button-primary full-width" (click)="api.sidebarOpen.set(false)">Ver carrito completo</a><button type="button" (click)="api.sidebarOpen.set(false)">Seguir explorando</button>
            </div>
          } @else { <div class="cart-drawer-empty"><p>Tu carrito está vacío.</p><a routerLink="/productos" class="button button-primary" (click)="api.sidebarOpen.set(false)">Explorar productos</a></div> }
        </aside>
      }
      <router-outlet />
      @if (!adminPage()) {
      <footer class="site-footer">
        <div class="container footer-main">
          <div class="footer-brand-column">
            <a routerLink="/" class="footer-brand" aria-label="TechStore Pro, inicio"><span class="brand-symbol" aria-hidden="true"></span><span>TechStore <b>Pro</b></span></a>
            <p>Accesorios para que cada día suene, se sienta y funcione mejor.</p>
          </div>
          <nav class="footer-column" aria-label="Enlaces de la tienda">
            <h2>Explorar</h2>
            <a routerLink="/">Inicio</a>
            <a routerLink="/productos">Productos</a>
            <a routerLink="/carrito">Carrito</a>
          </nav>
          <nav class="footer-column" aria-label="Accesos a tu cuenta">
            <h2>Tu espacio</h2>
            @if (auth.token()) {
              <a routerLink="/perfil">Mi cuenta</a>
              <a routerLink="/pedidos">Mis pedidos</a>
            } @else {
              <a routerLink="/login">Ingresar</a>
              <a routerLink="/registro">Crear cuenta</a>
            }
          </nav>
          <div class="footer-discover">
            <h2>Tu próximo favorito</h2>
            <p>Audio y periféricos para lo que viene.</p>
            <a routerLink="/productos" class="footer-discover-link"><span>Ver catálogo</span><span class="footer-discover-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" aria-hidden="true"><path d="M5 12h14m-6-6 6 6-6 6"/></svg></span></a>
          </div>
        </div>
        <div class="footer-bottom-wrap"><div class="container footer-bottom"><span>© {{ currentYear }} TechStore Pro</span><span>Hecho para explorar más.</span></div></div>
      </footer>
      }
    </div>
  `
})
export class App implements OnInit {
  private readonly router = inject(Router);
  readonly adminPage = toSignal(this.router.events.pipe(filter(event => event instanceof NavigationEnd),
    map(() => this.router.url.split('?')[0] === '/admin'), startWith(this.router.url.split('?')[0] === '/admin')));
  readonly auth = inject(AuthService);
  readonly api = inject(StoreApi);
  readonly money = money;
  readonly currentYear = new Date().getFullYear();
  readonly sidebarBusy = signal(false);
  readonly sidebarError = signal('');
  ngOnInit() {
    if (this.auth.token()) {
      this.auth.loadUser().subscribe({ next: () => this.api.restoreAfterLogin().subscribe({ error: () => {} }), error: () => this.auth.logout() });
    } else this.api.loadCart().subscribe();
  }
  @HostListener('document:keydown.escape') closeSidebar() { this.api.sidebarOpen.set(false); }
  asProduct(item: { productoId: number; nombre: string; categoria: string; imagenUrl: string | null; precio: number; stock: number }) {
    return { id: item.productoId, nombre: item.nombre, categoria: item.categoria, imagenUrl: item.imagenUrl,
      descripcion: '', precio: item.precio, stock: item.stock, destacado: false, activo: true };
  }
  removeFromSidebar(id: number) {
    this.sidebarBusy.set(true); this.sidebarError.set('');
    this.api.removeItem(id).subscribe({ next: () => this.sidebarBusy.set(false),
      error: () => { this.sidebarError.set('No se pudo quitar el producto.'); this.sidebarBusy.set(false); } });
  }
}
