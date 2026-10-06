import { Component, computed, DestroyRef, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpEventType, HttpResponse } from '@angular/common/http';
import { filter, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AdminDashboard, Order, Product, ProductInput, StoreApi, money, orderStatusLabel, nextOrderStatus } from '../../core/store-api';
import { OrderStepper } from '../../shared/order-stepper';
import { AuthService } from '../../core/auth.service';
import { ProductVisual } from '../../shared/product-visual';

const blankProduct = (): ProductInput => ({ nombre: '', descripcion: '', categoria: 'TECLADOS', precio: 0,
  stock: 0, imagenUrl: null, destacado: false, activo: true });

@Component({
  selector: 'app-admin',
  imports: [FormsModule, RouterLink, ProductVisual, OrderStepper],
  template: `
    <section class="admin-shell">
      <aside class="admin-sidebar">
        <a routerLink="/" class="admin-brand"><span class="brand-symbol" aria-hidden="true"></span><span>TechStore <b>Pro</b><small>Administración</small></span></a>
        <span class="sidebar-label">TIENDA</span>
        <nav aria-label="Secciones de administración">
          <button type="button" [class.active]="tab() === 'resumen'" [attr.aria-current]="tab() === 'resumen' ? 'page' : null" (click)="tab.set('resumen')"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="m3 10 9-7 9 7v10H3Z M9 20v-8h6v8"/></svg>Resumen</button>
          <button type="button" [class.active]="tab() === 'productos'" [attr.aria-current]="tab() === 'productos' ? 'page' : null" (click)="tab.set('productos')"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="m12 3 9 5v8l-9 5-9-5V8Zm-9 5 9 5 9-5M12 13v8M7.5 5.5l9 5"/></svg>Productos<span class="admin-nav-count">{{ dashboard()?.totalProductos }}</span></button>
          <button type="button" [class.active]="tab() === 'pedidos'" [attr.aria-current]="tab() === 'pedidos' ? 'page' : null" (click)="tab.set('pedidos')"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M8 4H5v17h14V4h-3M8 3h8v4H8ZM8 12h8M8 16h5"/></svg>Pedidos@if (pendingOrders()) { <span class="admin-nav-count">{{ pendingOrders() }}</span> }</button>
          <a routerLink="/"><svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="4" width="18" height="13" rx="2"/><path d="M8 21h8M12 17v4"/></svg>Ver tienda<svg class="admin-external" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 17 17 7M7 7h10v10"/></svg></a>
        </nav>
        <div class="admin-account"><span class="sidebar-label">CUENTA</span><a routerLink="/perfil"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="3"/><path d="M5 21v-2a7 7 0 0 1 14 0v2"/></svg>Mi perfil</a><button type="button" (click)="logout()"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M9 4H4v16h5M9 12h12m-4-4 4 4-4 4"/></svg>Cerrar sesión</button></div>
        <div class="admin-sidebar-footer"><span class="admin-avatar">{{ auth.user()?.nombre?.charAt(0) || 'A' }}</span><div><strong>{{ auth.user()?.nombre || 'Administrador' }}</strong><small>Administrador de tienda</small></div></div>
      </aside>
      <div class="admin-content">
        <header class="admin-header"><div><span class="admin-eyebrow">PANEL DE CONTROL</span><h1>{{ tab() === 'resumen' ? 'Resumen de la tienda' : tab() === 'productos' ? 'Productos' : 'Pedidos' }}</h1><p>{{ tab() === 'resumen' ? 'Una vista de tu catálogo y las compras confirmadas.' : tab() === 'productos' ? 'Gestiona el catálogo y su disponibilidad.' : 'Consulta y actualiza el estado de las compras.' }}</p></div><div class="admin-header-actions"><button type="button" class="admin-icon-button" [disabled]="loading()" aria-label="Actualizar datos" title="Actualizar datos" (click)="load()"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20 7v5h-5M4 17v-5h5M6 7a7 7 0 0 1 12-1l2 6M4 12l2 6a7 7 0 0 0 12-1"/></svg></button><a routerLink="/perfil" class="admin-icon-button" aria-label="Mi perfil" title="Mi perfil"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="3"/><path d="M5 21v-2a7 7 0 0 1 14 0v2"/></svg></a></div></header>
        @if (error()) { <div class="admin-alert" role="alert">{{ error() }} <button (click)="load()">Reintentar</button></div> }
        @if (notice()) { <div class="admin-success" role="status">{{ notice() }}</div> }
        @if (loading()) { <p class="admin-loading" role="status">Actualizando datos de la tienda…</p> }
        @if (tab() === 'resumen' && !loading() && !error() && dashboard()) {
          <div class="admin-metrics">
            <article class="admin-metric"><span class="metric-icon blue"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3v18M17 6H9a3 3 0 0 0 0 6h6a3 3 0 0 1 0 6H6"/></svg></span><h2>Importe de pedidos</h2><strong>{{ money(dashboard()!.importePedidos) }}</strong><p>Total de compras confirmadas</p></article>
            <article class="admin-metric"><span class="metric-icon green"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M16 5a3 3 0 0 1 0 6M21 21v-3a6 6 0 0 0-3-5"/></svg></span><h2>Clientes con pedidos</h2><strong>{{ dashboard()!.clientesConPedidos }}</strong><p>Usuarios con al menos una compra</p></article>
            <article class="admin-metric"><span class="metric-icon purple"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 3h2l3 12h11l2-8H6"/><circle cx="9" cy="20" r="1"/><circle cx="18" cy="20" r="1"/></svg></span><h2>Pedidos</h2><strong>{{ dashboard()?.totalPedidos }}</strong><p>{{ pendingOrders() }} pendientes de entrega</p></article>
            <article class="admin-metric"><span class="metric-icon amber"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="m12 3 9 5v8l-9 5-9-5V8Zm-9 5 9 5 9-5M12 13v8"/></svg></span><h2>Productos visibles</h2><strong>{{ dashboard()!.productosVisibles }}</strong><p>{{ dashboard()?.totalProductos }} registrados en el catálogo</p></article>
          </div>
          <div class="admin-dashboard-grid">
            <section class="admin-panel recent-orders"><div class="admin-panel-heading"><h2>Pedidos recientes</h2><button type="button" (click)="tab.set('pedidos')">Ver todos <span aria-hidden="true">→</span></button></div>
              @for (order of dashboard()!.pedidosRecientes; track order.id) {
                <button class="admin-activity" type="button" (click)="tab.set('pedidos')"><span class="activity-icon"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M8 4H5v17h14V4h-3M8 3h8v4H8ZM8 12h8M8 16h5"/></svg></span><span class="activity-copy"><strong>Pedido #{{ order.id }} <b>{{ money(order.total) }}</b></strong><span>Cliente #{{ order.usuarioId }} · {{ statusLabel(order.estado) }}</span><time [attr.datetime]="order.createdAt">{{ date(order.createdAt) }}</time></span><span class="activity-arrow" aria-hidden="true">↗</span></button>
              } @empty { <div class="dashboard-empty"><span class="empty-symbol" aria-hidden="true">↗</span><h3>Todo listo para tu primera venta</h3><p>Los pedidos confirmados aparecerán aquí.</p><a routerLink="/productos">Ver el catálogo →</a></div> }
              <div class="admin-inventory-summary"><span><i class="inventory-dot"></i>Disponibilidad del catálogo</span><strong>{{ dashboard()!.productosConStock }} con stock <span>· {{ dashboard()!.productosAgotados }} agotados</span></strong><button type="button" (click)="tab.set('productos')">Gestionar productos →</button></div>
            </section>
            <div class="admin-dashboard-side">
              <section class="admin-panel"><div class="admin-panel-heading"><h2>Estado de los pedidos</h2><span>{{ dashboard()?.totalPedidos }} total</span></div><div class="admin-status-list">@for (item of statusSummary(); track item.value) { <div class="admin-status-item"><div><span>{{ item.label }}</span><strong>{{ item.count }}</strong></div><div class="admin-status-track" role="meter" [attr.aria-label]="item.label" aria-valuemin="0" [attr.aria-valuemax]="dashboard()?.totalPedidos || 1" [attr.aria-valuenow]="item.count"><span [style.width.%]="item.percent" [style.background]="item.color"></span></div></div> }</div></section>
              <section class="admin-panel"><div class="admin-panel-heading"><h2>Productos más pedidos</h2></div><div class="admin-top-products">@for (product of dashboard()!.productosMasPedidos; track product.id) { <div><span><strong>{{ product.nombre }}</strong><small>{{ product.unidades }} {{ product.unidades === 1 ? 'unidad' : 'unidades' }}</small></span><b>{{ money(product.importe) }}</b></div> } @empty { <p class="admin-small-empty">Cuando recibas pedidos, verás aquí los productos con más unidades vendidas.</p> }</div></section>
            </div>
          </div>
        } @else if (tab() === 'productos') {
          <div class="admin-section-head"><div><strong>{{ dashboard()?.totalProductos }}</strong><span>productos registrados</span></div><button class="button button-primary" type="button" (click)="newProduct()">+ Nuevo producto</button></div>
          @if (showForm()) {
            <form class="admin-form" #productForm="ngForm" (ngSubmit)="save()">
              <div class="form-head"><h3>{{ editingId() ? 'Editar producto' : 'Nuevo producto' }}</h3><button type="button" [disabled]="saving()" (click)="closeForm()">Cerrar</button></div>
              <fieldset class="product-form-fields" [disabled]="saving()">
              <div class="form-grid">
                <label>Nombre<input name="nombre" [(ngModel)]="model.nombre" required maxlength="140"></label>
                <label>Categoría<select name="categoria" [(ngModel)]="model.categoria" required><option value="TECLADOS">Teclados</option><option value="MOUSE">Mouse</option><option value="AUDIFONOS">Audífonos</option><option value="AUDIO">Audio</option><option value="ACCESORIOS">Accesorios</option></select></label>
                <label>Precio (S/)<input name="precio" type="number" min="0" step="0.01" [(ngModel)]="model.precio" required></label>
                <label>Stock<input name="stock" type="number" min="0" step="1" [(ngModel)]="model.stock" required></label>
                <label class="span-2">Descripción<textarea name="descripcion" [(ngModel)]="model.descripcion" required rows="3"></textarea></label>
                <div class="span-2 image-upload">
                  <div class="image-upload-heading"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M7 18H6a4 4 0 0 1-.5-8A6.5 6.5 0 0 1 18 8a5 5 0 0 1 0 10h-1M12 20V11m-4 4 4-4 4 4"/></svg><div><h4>Imagen del producto</h4><p>Selecciona una imagen para mostrarla en la tienda.</p></div></div>
                  <div class="image-dropzone" [class.dragging]="dragging()" (dragover)="onDragOver($event)" (dragleave)="dragging.set(false)" (drop)="onDrop($event)">
                    <input #imageInput type="file" accept="image/jpeg,image/png,image/webp" class="image-file-input" aria-label="Seleccionar imagen del producto" (change)="onFileChange($event)">
                    <svg class="image-upload-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M7 18H6a4 4 0 0 1-.5-8A6.5 6.5 0 0 1 18 8a5 5 0 0 1 0 10h-1M12 20V11m-4 4 4-4 4 4"/></svg>
                    <strong>Arrastra tu imagen aquí</strong><p>JPEG, PNG o WebP · Hasta 5 MB</p>
                    <button class="image-browse" type="button" (click)="imageInput.click()">Seleccionar archivo</button>
                  </div>
                  @if (preview() || model.imagenUrl) {
                    <div class="image-upload-file"><img [src]="preview() || model.imagenUrl" alt="Vista previa de la imagen del producto"><div><strong>{{ selectedFile()?.name || 'Imagen actual' }}</strong><p role="status">{{ uploading() ? 'Subiendo imagen… ' + progress() + '%' : selectedFile() ? fileSize(selectedFile()!.size) + ' · Lista para guardar' : 'Imagen guardada' }}</p>@if (uploading()) { <progress [value]="progress()" max="100" aria-label="Progreso de subida"></progress> }</div><button type="button" aria-label="Quitar imagen del producto" (click)="removeImage()"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" aria-hidden="true"><path d="m6 6 12 12M18 6 6 18"/></svg></button></div>
                  }
                  @if (imageError()) { <p class="image-upload-error" role="alert">{{ imageError() }}</p> }
                </div>
              </div>
              <div class="form-checks"><label><input type="checkbox" name="destacado" [(ngModel)]="model.destacado"> Destacado</label><label><input type="checkbox" name="activo" [(ngModel)]="model.activo"> Visible en tienda</label></div>
              </fieldset>
              <button class="button button-primary" type="submit" [disabled]="productForm.invalid || saving()">{{ saving() ? 'Guardando…' : 'Guardar producto' }}</button>
            </form>
          }
          <div class="admin-product-list">
            @for (product of products(); track product.id) {
              <article class="admin-product-row"><div class="admin-thumb"><app-product-visual [product]="product" /></div><div class="admin-product-name"><strong>{{ product.nombre }}</strong><span>{{ product.categoria }} · {{ product.activo ? 'Visible' : 'Oculto' }}</span><small class="admin-mobile-product-meta">{{ money(product.precio) }} · {{ product.stock }} en stock</small></div><span class="admin-price">{{ money(product.precio) }}</span><span class="admin-stock">{{ product.stock }} en stock</span><div class="admin-row-actions"><button type="button" (click)="edit(product)">Editar</button><button type="button" (click)="deactivate(product)" [disabled]="!product.activo">Ocultar</button></div></article>
            } @empty { <p class="admin-empty">Todavía no hay productos. Crea el primero para mostrarlo en la tienda.</p> }
          </div>
        } @else if (tab() === 'pedidos') {
          <div class="admin-section-head"><div><strong>{{ dashboard()?.totalPedidos }}</strong><span>pedidos confirmados</span></div></div>
          <div class="admin-orders-list">@for (order of orders(); track order.id) {
            <article class="admin-order"><div><strong>Pedido #{{ order.id }}</strong><span>{{ date(order.createdAt) }} · Usuario #{{ order.usuarioId }}</span></div><div><span class="status-pill">{{ statusLabel(order.estado) }}</span><strong>{{ money(order.total) }}</strong></div>
              <app-order-stepper [status]="order.estado" />
              <ul>@for (item of order.items; track item.productoId) { <li>{{ item.nombre }} × {{ item.cantidad }}</li> }</ul>
              <div class="order-status-actions">
                @if (nextStatus(order.estado); as next) {
                  <p>Siguiente paso: <strong>{{ statusLabel(next) }}</strong></p>
                  <button class="button button-primary" type="button" [disabled]="updatingOrders().includes(order.id)" (click)="advanceOrder(order)">{{ updatingOrders().includes(order.id) ? 'Actualizando…' : 'Marcar como ' + statusLabel(next).toLowerCase() }}</button>
                } @else { <p>Pedido entregado. El proceso ha finalizado.</p> }
              </div>
            </article>
          } @empty { <p class="admin-empty">Aún no hay pedidos confirmados.</p> }</div>
        }
      </div>
    </section>
  `
})
export class AdminPage implements OnInit, OnDestroy {
  private readonly api = inject(StoreApi);
  private readonly destroyRef = inject(DestroyRef);
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly tab = signal<'resumen' | 'productos' | 'pedidos'>('resumen');
  readonly loading = signal(false);
  readonly products = signal<Product[]>([]);
  readonly orders = signal<Order[]>([]);
  readonly dashboard = signal<AdminDashboard | null>(null);
  readonly pendingOrders = computed(() => this.dashboard()?.pedidosPendientes ?? 0);
  // Colors are presentation only; amounts, counts, percentages and ranking come from the API.
  readonly statusSummary = computed(() => (this.dashboard()?.estados ?? []).map(item => ({ ...item,
    color: ({ CONFIRMADO: '#3988ff', EN_PREPARACION: '#a477f4', ENVIADO: '#f3a84e', ENTREGADO: '#25c993' })[item.value]
  })));
  readonly showForm = signal(false);
  readonly editingId = signal<number | null>(null);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly selectedFile = signal<File | null>(null);
  readonly preview = signal('');
  readonly dragging = signal(false);
  readonly uploading = signal(false);
  readonly progress = signal(0);
  readonly imageError = signal('');
  readonly money = money;
  readonly statusLabel = orderStatusLabel;
  readonly nextStatus = nextOrderStatus;
  readonly updatingOrders = signal<number[]>([]);
  advanceOrder(order: Order) {
    const next = nextOrderStatus(order.estado);
    if (!next || this.updatingOrders().includes(order.id)) return;
    this.updatingOrders.update(ids => [...ids, order.id]);
    this.error.set(''); this.notice.set('');
    this.api.updateOrderStatus(order, next).subscribe({
      next: updated => {
        this.orders.update(orders => orders.map(item => item.id === updated.id ? updated : item));
        this.updatingOrders.update(ids => ids.filter(id => id !== order.id));
        this.notice.set(`Pedido #${order.id}: ${orderStatusLabel(updated.estado)}.`);
        this.load();
      },
      error: error => {
        this.updatingOrders.update(ids => ids.filter(id => id !== order.id));
        this.error.set(error.status === 409 ? 'El estado del pedido cambió. Pulsa Reintentar para actualizar la lista.' : 'No se pudo actualizar el estado. Pulsa Reintentar y vuelve a intentarlo.');
      }
    });
  }
  readonly date = (value: string) => new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));
  model = blankProduct();
  ngOnInit() { this.load(); }
  load() {
    if (this.loading()) return;
    this.error.set('');
    this.loading.set(true);
    forkJoin({ products: this.api.adminProducts(), orders: this.api.allOrders(), dashboard: this.api.adminDashboard() }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: data => { this.products.set(data.products); this.orders.set(data.orders); this.dashboard.set(data.dashboard); this.loading.set(false); },
      error: () => { this.loading.set(false); this.error.set('No se pudieron actualizar los datos de la tienda. Inténtalo de nuevo.'); }
    });
  }
  logout() { this.auth.logout(); void this.router.navigateByUrl('/login'); }
  newProduct() { if (this.saving()) return; this.tab.set('productos'); this.clearSelection(); this.model = blankProduct(); this.editingId.set(null); this.showForm.set(true); this.notice.set(''); }
  edit(product: Product) { if (this.saving()) return; this.clearSelection(); this.model = { nombre: product.nombre, descripcion: product.descripcion, categoria: product.categoria,
    precio: product.precio, stock: product.stock, imagenUrl: product.imagenUrl, destacado: product.destacado, activo: product.activo };
    this.editingId.set(product.id); this.showForm.set(true); this.notice.set(''); window.scrollTo({ top: 0, behavior: 'smooth' }); }
  save() {
    if (this.saving()) return;
    this.saving.set(true); this.error.set(''); this.notice.set('');
    this.imageError.set('');
    const file = this.selectedFile();
    this.uploading.set(!!file); this.progress.set(0);
    const image = file ? this.api.uploadProductImage(file).pipe(
      tap(event => { if (event.type === HttpEventType.UploadProgress) this.progress.set(Math.round(100 * event.loaded / (event.total || file.size))); }),
      filter(event => event instanceof HttpResponse),
      map(event => event.body!.imagenUrl),
      tap(url => { this.model.imagenUrl = url; this.uploading.set(false); this.clearSelection(); })
    ) : of(this.model.imagenUrl);
    image.pipe(switchMap(() => this.editingId() ? this.api.updateProduct(this.editingId()!, this.model) : this.api.createProduct(this.model)), takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: () => { this.saving.set(false); this.showForm.set(false); this.clearSelection(); this.notice.set('Producto guardado.'); this.load(); },
      error: error => { const wasUploading = this.uploading(); this.uploading.set(false); this.saving.set(false); const message = error.status === 413 ? 'La imagen supera el límite de 5 MB.' : wasUploading ? 'No se pudo subir la imagen. Inténtalo de nuevo.' : 'No se pudo guardar el producto. Revisa los datos.'; if (wasUploading) this.imageError.set(message); else this.error.set(message); } });
  }
  onFileChange(event: Event) { const input = event.target as HTMLInputElement; if (input.files?.length) this.chooseFile(input.files); input.value = ''; }
  onDragOver(event: DragEvent) { event.preventDefault(); if (!this.saving()) this.dragging.set(true); }
  onDrop(event: DragEvent) { event.preventDefault(); this.dragging.set(false); if (event.dataTransfer?.files.length) this.chooseFile(event.dataTransfer.files); }
  private chooseFile(files: FileList) {
    if (this.saving()) return;
    this.imageError.set('');
    if (files.length !== 1) { this.imageError.set('Selecciona una sola imagen para este producto.'); return; }
    const file = files[0];
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) { this.imageError.set('Usa una imagen JPEG, PNG o WebP.'); return; }
    if (!file.size || file.size > 5 * 1024 * 1024) { this.imageError.set('Selecciona una imagen de hasta 5 MB.'); return; }
    this.clearSelection(); this.selectedFile.set(file); this.preview.set(URL.createObjectURL(file));
  }
  private clearSelection() { if (this.preview()) URL.revokeObjectURL(this.preview()); this.preview.set(''); this.selectedFile.set(null); this.imageError.set(''); }
  removeImage() { if (this.saving()) return; this.clearSelection(); this.model.imagenUrl = null; }
  closeForm() { if (this.saving()) return; this.clearSelection(); this.showForm.set(false); }
  fileSize(bytes: number) { return bytes < 1024 * 1024 ? Math.ceil(bytes / 1024) + ' KB' : (bytes / (1024 * 1024)).toFixed(1) + ' MB'; }
  ngOnDestroy() { this.clearSelection(); }
  deactivate(product: Product) {
    if (!window.confirm(`¿Ocultar ${product.nombre} de la tienda?`)) return;
    this.api.deleteProduct(product.id).subscribe({ next: () => { this.notice.set('Producto oculto.'); this.load(); },
      error: () => this.error.set('No se pudo ocultar el producto.') });
  }
}
