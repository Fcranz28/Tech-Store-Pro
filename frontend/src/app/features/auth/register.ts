import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  template: `
    <section class="auth-page"><div class="auth-art"><div class="auth-art-copy"><h2>Algo bueno<br>empieza aquí.</h2><p>Guarda tus favoritos y compra sin perder el ritmo.</p></div></div>
      <div class="auth-panel"><a routerLink="/" class="auth-back">← Volver a la tienda</a><h1>Crea tu cuenta.</h1><p>Tu próxima gran elección empieza aquí.</p>
      <form (ngSubmit)="submit()" class="auth-form">
        <label>Nombre
          <input name="nombre" type="text" autocomplete="name" required maxlength="100" [(ngModel)]="nombre" placeholder="Tu nombre">
        </label>
        <label>Correo electrónico
          <input name="email" type="email" autocomplete="email" required [(ngModel)]="email" placeholder="nombre@ejemplo.com">
        </label>
        <label>Contraseña <small>mínimo 8 caracteres</small>
          <input name="password" type="password" autocomplete="new-password" required minlength="8" [(ngModel)]="password" placeholder="Crea una contraseña">
        </label>
        @if (error()) { <p role="alert" class="form-error">{{ error() }}</p> }
        <button type="submit" [disabled]="loading()" class="button button-primary full-width">{{ loading() ? 'Creando cuenta…' : 'Crear cuenta' }}</button>
      </form>
      <p class="auth-switch">¿Ya tienes cuenta? <a routerLink="/login" [queryParams]="{ next: route.snapshot.queryParamMap.get('next') || '/' }">Iniciar sesión</a></p></div>
    </section>
  `
})
export class Register {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly route = inject(ActivatedRoute);
  nombre = '';
  email = '';
  password = '';
  readonly error = signal('');
  readonly loading = signal(false);

  submit() {
    this.error.set('');
    this.loading.set(true);
    this.auth.register(this.nombre, this.email, this.password).subscribe({
      next: () => void this.router.navigateByUrl(this.route.snapshot.queryParamMap.get('next') || '/'),
      error: (error: HttpErrorResponse) => {
        this.error.set(error.status === 409 ? 'Este correo ya está registrado.' : 'No se pudo crear la cuenta. Revisa los datos.');
        this.loading.set(false);
      }
    });
  }
}
