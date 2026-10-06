import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, of, switchMap, tap, throwError } from 'rxjs';

export const API_URL = 'http://localhost:8080';
export const money = (value: number) => new Intl.NumberFormat('es-PE', {
  style: 'currency', currency: 'PEN', maximumFractionDigits: 2
}).format(value);

export interface Product {
  id: number;
  nombre: string;
  descripcion: string;
  categoria: string;
  precio: number;
  stock: number;
  imagenUrl: string | null;
  destacado: boolean;
  activo: boolean;
}

export type ProductInput = Omit<Product, 'id'>;
export interface CatalogResponse {
  productos: Product[]; total: number; pagina: number; totalPaginas: number; tamanio: number; totalCatalogo: number;
  categorias: { value: string; label: string; count: number }[];
}

export interface CartItem {
  id: number;
  productoId: number;
  nombre: string;
  categoria: string;
  imagenUrl: string | null;
  precio: number;
  stock: number;
  cantidad: number;
  subtotal: number;
}

export interface Cart { items: CartItem[]; total: number }
interface GuestCartResponse { token: string; carrito: Cart }
const GUEST_CART_KEY = 'techstore_guest_cart_jwt';

export interface OrderLine {
  productoId: number;
  nombre: string;
  precioUnitario: number;
  cantidad: number;
  subtotal: number;
}
export interface Order {
  id: number;
  usuarioId: number;
  estado: OrderStatus;
  total: number;
  createdAt: string;
  items: OrderLine[];
}

export type OrderStatus = 'CONFIRMADO' | 'EN_PREPARACION' | 'ENVIADO' | 'ENTREGADO';
export interface AdminDashboard {
  importePedidos: number;
  clientesConPedidos: number;
  totalPedidos: number;
  pedidosPendientes: number;
  totalProductos: number;
  productosVisibles: number;
  productosConStock: number;
  productosAgotados: number;
  pedidosRecientes: Pick<Order, 'id' | 'usuarioId' | 'estado' | 'total' | 'createdAt'>[];
  estados: { value: OrderStatus; label: string; count: number; percent: number }[];
  productosMasPedidos: { id: number; nombre: string; unidades: number; importe: number }[];
}
export const ORDER_STEPS: { value: OrderStatus; label: string }[] = [
  { value: 'CONFIRMADO', label: 'Confirmado' }, { value: 'EN_PREPARACION', label: 'En preparación' },
  { value: 'ENVIADO', label: 'Enviado' }, { value: 'ENTREGADO', label: 'Entregado' }
];
export const orderStatusLabel = (status: OrderStatus) => ORDER_STEPS.find(step => step.value === status)?.label ?? status;
export const nextOrderStatus = (status: OrderStatus) => ORDER_STEPS[ORDER_STEPS.findIndex(step => step.value === status) + 1]?.value;

@Injectable({ providedIn: 'root' })
export class StoreApi {
  private readonly http = inject(HttpClient);
  readonly cart = signal<Cart>({ items: [], total: 0 });
  readonly sidebarOpen = signal(false);
  readonly transferError = signal('');

  products() { return this.http.get<Product[]>(`${API_URL}/productos`); }
  catalog(params: Record<string, string>) { return this.http.get<CatalogResponse>(`${API_URL}/productos/catalogo`, { params }); }
  product(id: number) { return this.http.get<Product>(`${API_URL}/productos/${id}`); }
  adminProducts() { return this.http.get<Product[]>(`${API_URL}/productos/admin`); }
  createProduct(input: ProductInput) { return this.http.post<Product>(`${API_URL}/productos`, input); }
  updateProduct(id: number, input: ProductInput) { return this.http.put<Product>(`${API_URL}/productos/${id}`, input); }
  deleteProduct(id: number) { return this.http.delete<void>(`${API_URL}/productos/${id}`); }
  uploadProductImage(file: File) {
    const body = new FormData();
    body.append('file', file);
    return this.http.post<{ imagenUrl: string }>(`${API_URL}/productos/imagenes`, body, { observe: 'events', reportProgress: true });
  }

  private guestToken() { return sessionStorage.getItem(GUEST_CART_KEY); }
  private saveGuest(response: GuestCartResponse) {
    sessionStorage.setItem(GUEST_CART_KEY, response.token);
    this.cart.set(response.carrito);
  }
  private authenticated() { return !!sessionStorage.getItem('techstore_token'); }
  loadCart() {
    if (this.authenticated()) {
      return this.http.get<Cart>(`${API_URL}/carrito`).pipe(tap(cart => this.cart.set(cart)));
    }
    if (!this.guestToken()) {
      const empty = { items: [], total: 0 };
      this.cart.set(empty);
      return of(empty);
    }
    return this.http.post<GuestCartResponse>(`${API_URL}/carrito/invitado/actual`, { token: this.guestToken() }).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status !== 401) return throwError(() => error);
        sessionStorage.removeItem(GUEST_CART_KEY);
        return this.http.post<GuestCartResponse>(`${API_URL}/carrito/invitado/actual`, { token: null });
      }))
      .pipe(tap(response => this.saveGuest(response)), switchMap(response => of(response.carrito)));
  }
  addToCart(productoId: number, cantidad = 1) {
    if (this.authenticated() && !this.transferError()) {
      return this.http.post<Cart>(`${API_URL}/carrito/items`, { productoId, cantidad })
        .pipe(tap(cart => { this.cart.set(cart); this.sidebarOpen.set(true); }));
    }
    return this.http.post<GuestCartResponse>(`${API_URL}/carrito/invitado/items`, { token: this.guestToken(), productoId, cantidad })
      .pipe(tap(response => { this.saveGuest(response); this.sidebarOpen.set(true); }), switchMap(response => of(response.carrito)));
  }
  changeQuantity(id: number, cantidad: number) {
    if (this.authenticated() && !this.transferError()) {
      return this.http.put<Cart>(`${API_URL}/carrito/items/${id}`, { cantidad })
        .pipe(tap(cart => this.cart.set(cart)));
    }
    return this.http.put<GuestCartResponse>(`${API_URL}/carrito/invitado/items/${id}`, { token: this.guestToken(), cantidad })
      .pipe(tap(response => this.saveGuest(response)), switchMap(response => of(response.carrito)));
  }
  removeItem(id: number) {
    if (this.authenticated() && !this.transferError()) {
      return this.http.delete<void>(`${API_URL}/carrito/items/${id}`).pipe(switchMap(() => this.loadCart()));
    }
    return this.http.post<GuestCartResponse>(`${API_URL}/carrito/invitado/items/${id}/eliminar`, { token: this.guestToken() })
      .pipe(tap(response => this.saveGuest(response)), switchMap(response => of(response.carrito)));
  }
  restoreAfterLogin() {
    if (!this.authenticated()) return this.loadCart();
    const token = this.guestToken();
    if (!token) return this.loadCart();
    return this.http.post<Cart>(`${API_URL}/carrito/invitado/transferir`, { token }).pipe(tap(cart => {
      sessionStorage.removeItem(GUEST_CART_KEY);
      this.transferError.set('');
      this.cart.set(cart);
    }), catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        sessionStorage.removeItem(GUEST_CART_KEY);
        this.transferError.set('');
        return this.loadCart();
      }
      this.transferError.set('No pudimos recuperar tu carrito. Inténtalo desde el carrito.');
      return throwError(() => error);
    }));
  }
  confirmOrder() { return this.http.post<Order>(`${API_URL}/pedidos`, {}).pipe(tap(() => this.cart.set({ items: [], total: 0 }))); }
  myOrders() { return this.http.get<Order[]>(`${API_URL}/pedidos`); }
  allOrders() { return this.http.get<Order[]>(`${API_URL}/pedidos/admin`); }
  adminDashboard() { return this.http.get<AdminDashboard>(`${API_URL}/admin/resumen`); }
  updateOrderStatus(order: Order, estado: OrderStatus) {
    return this.http.patch<Order>(`${API_URL}/pedidos/admin/${order.id}/estado`, { estadoActual: order.estado, estado });
  }
}
