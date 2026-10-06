import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { catchError, map, of } from 'rxjs';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.token()) return router.createUrlTree(['/login']);
  if (auth.user()?.rol === 'ADMIN') return true;
  return auth.loadUser().pipe(
    map(user => user.rol === 'ADMIN' ? true : router.createUrlTree(['/'])),
    catchError(() => of(router.createUrlTree(['/login'])))
  );
};
