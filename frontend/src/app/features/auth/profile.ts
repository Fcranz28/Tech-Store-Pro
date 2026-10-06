import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-profile',
  template: `
    <section class="container inner-page"><div class="profile-panel">
      <h1>Mi cuenta</h1>
      @if (error()) { <p role="alert" class="form-error">{{ error() }}</p> }
      @else if (auth.user(); as user) {
        <p>Hola, <strong>{{ user.nombre }}</strong>.</p>
        <dl><div><dt>Correo</dt><dd>{{ user.email }}</dd></div><div><dt>Cuenta</dt><dd>{{ user.rol === 'ADMIN' ? 'Administrador' : 'Cliente' }}</dd></div></dl>
      } @else { <p>Cargando perfil…</p> }
      <button (click)="logout()" class="button button-outline">Cerrar sesión</button></div>
    </section>
  `
})
export class Profile implements OnInit {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly error = signal('');

  ngOnInit() {
    this.auth.loadUser().subscribe({
      error: () => {
        this.auth.logout();
        this.error.set('La sesión caducó. Inicia sesión de nuevo.');
        void this.router.navigateByUrl('/login');
      }
    });
  }

  logout() {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }
}
