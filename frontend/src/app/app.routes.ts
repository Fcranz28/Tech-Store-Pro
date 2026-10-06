import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { adminGuard } from './core/admin.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/inicio/home').then(m => m.Home) },
  { path: 'productos', loadComponent: () => import('./features/catalogo/catalog').then(m => m.Catalog) },
  { path: 'productos/:id', loadComponent: () => import('./features/catalogo/product-detail').then(m => m.ProductDetail) },
  { path: 'login', loadComponent: () => import('./features/auth/login').then(m => m.Login) },
  { path: 'registro', loadComponent: () => import('./features/auth/register').then(m => m.Register) },
  { path: 'perfil', canActivate: [authGuard], loadComponent: () => import('./features/auth/profile').then(m => m.Profile) },
  { path: 'carrito', loadComponent: () => import('./features/carrito/cart').then(m => m.CartPage) },
  { path: 'pedidos', canActivate: [authGuard], loadComponent: () => import('./features/pedidos/orders').then(m => m.OrdersPage) },
  { path: 'admin', canActivate: [adminGuard], loadComponent: () => import('./features/admin/admin').then(m => m.AdminPage) },
  { path: '**', redirectTo: '' }
];
