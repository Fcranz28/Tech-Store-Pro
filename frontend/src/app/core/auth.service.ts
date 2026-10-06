import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, map, of, switchMap, tap } from 'rxjs';
import { StoreApi } from './store-api';

export interface User {
  id: number;
  nombre: string;
  email: string;
  rol: 'USER' | 'ADMIN';
}

interface AuthResponse {
  token: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: User;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly cartApi = inject(StoreApi);
  private readonly api = 'http://localhost:8080/auth';
  readonly user = signal<User | null>(null);
  readonly token = signal<string | null>(sessionStorage.getItem('techstore_token'));

  register(nombre: string, email: string, password: string) {
    return this.http.post<AuthResponse>(`${this.api}/register`, { nombre, email, password })
      .pipe(tap(response => this.save(response)), switchMap(response => this.cartApi.restoreAfterLogin().pipe(
        map(() => response), catchError(() => { this.cartApi.transferError.set('No pudimos recuperar tu carrito. Inténtalo desde el carrito.'); return of(response); })
      )));
  }

  login(email: string, password: string) {
    return this.http.post<AuthResponse>(`${this.api}/login`, { email, password })
      .pipe(tap(response => this.save(response)), switchMap(response => this.cartApi.restoreAfterLogin().pipe(
        map(() => response), catchError(() => { this.cartApi.transferError.set('No pudimos recuperar tu carrito. Inténtalo desde el carrito.'); return of(response); })
      )));
  }

  loadUser() {
    return this.http.get<User>(`${this.api}/me`).pipe(tap(user => this.user.set(user)));
  }

  logout() {
    sessionStorage.removeItem('techstore_token');
    this.token.set(null);
    this.user.set(null);
    this.cartApi.cart.set({ items: [], total: 0 });
    this.cartApi.sidebarOpen.set(false);
    this.cartApi.transferError.set('');
    this.cartApi.loadCart().subscribe({ error: () => {} });
  }

  private save(response: AuthResponse) {
    sessionStorage.setItem('techstore_token', response.token);
    this.token.set(response.token);
    this.user.set(response.user);
  }
}
