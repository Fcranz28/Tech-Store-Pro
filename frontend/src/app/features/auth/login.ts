import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  template: `
    <section class="auth-page login-page" aria-labelledby="login-title">
      <div class="auth-art login-art">
        <a routerLink="/" class="login-back" aria-label="Volver a la tienda"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M19 12H5m7-7-7 7 7 7"/></svg></a>
        <div class="auth-art-copy"><h2>Tu tecnología.<br>Tu ritmo.</h2><p>Vuelve a tu selección y encuentra lo que sigue.</p></div>
      </div>
      <div class="auth-panel login-panel">
        <div class="login-form-wrap">
          <a routerLink="/" class="login-mobile-back">← Volver a la tienda</a>
          <h1 id="login-title">Bienvenido de nuevo.</h1>
          <p>Ingresa a tu cuenta para continuar.</p>
          <form (ngSubmit)="submit()" class="auth-form">
            <label for="login-email">Correo electrónico</label>
            <input id="login-email" name="email" type="email" autocomplete="email" required [(ngModel)]="email" placeholder="nombre@ejemplo.com">
            <label for="login-password">Contraseña</label>
            <div class="login-password-wrap">
              <input id="login-password" name="password" [type]="showPassword() ? 'text' : 'password'" autocomplete="current-password" required [(ngModel)]="password" placeholder="Tu contraseña">
              <button type="button" class="login-password-toggle" (click)="showPassword.set(!showPassword())" [attr.aria-label]="showPassword() ? 'Ocultar contraseña' : 'Mostrar contraseña'" [attr.aria-pressed]="showPassword()">
                @if (showPassword()) { <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 3 21 21M10.6 6.2A10.5 10.5 0 0 1 12 6c6 0 9.5 6 9.5 6a15 15 0 0 1-3.2 3.8M6.1 6.8C3.8 8.4 2.5 12 2.5 12S6 18 12 18c1.1 0 2.1-.2 3-.5"/><path d="M10 10a2.8 2.8 0 0 0 4 4"/></svg> }
                @else { <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z"/><circle cx="12" cy="12" r="2.8"/></svg> }
              </button>
            </div>
            @if (error()) { <p role="alert" class="form-error">{{ error() }}</p> }
            <button type="submit" [disabled]="loading()" class="button button-primary full-width login-submit">{{ loading() ? 'Ingresando…' : 'Iniciar sesión' }}</button>
          </form>
          <p class="auth-switch">¿Aún no tienes cuenta? <a routerLink="/registro" [queryParams]="{ next: route.snapshot.queryParamMap.get('next') || '/' }">Crear cuenta</a></p>
        </div>
      </div>
    </section>
  `
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly route = inject(ActivatedRoute);
  email = '';
  password = '';
  readonly error = signal('');
  readonly loading = signal(false);
  readonly showPassword = signal(false);

  submit() {
    this.error.set('');
    this.loading.set(true);
    this.auth.login(this.email, this.password).subscribe({
      next: () => void this.router.navigateByUrl(this.route.snapshot.queryParamMap.get('next') || '/'),
      error: (error: HttpErrorResponse) => {
        this.error.set(error.status === 401 ? 'Correo o contraseña incorrectos.' : 'No se pudo iniciar sesión. Inténtalo de nuevo.');
        this.loading.set(false);
      }
    });
  }
}
