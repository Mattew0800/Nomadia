import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError, EMPTY } from 'rxjs';
import { BackendStatusService } from '../services/Backend-Status/backend-status.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const router = inject(Router);
  const backendStatus = inject(BackendStatusService);

  return next(req).pipe(

    catchError((error: HttpErrorResponse) => {
      const loginApiUrl = '/login';

      if (error.status === 0 || error.status === 502 || error.status === 503 || error.status === 504) {
        console.error('Interceptor: Backend no disponible. Redirigiendo a página de error...');
        backendStatus.setBackendAvailable(false);

        // No redirigir si es el propio health check (evita loop)
        if (req.url.includes('/nomadia/health')) {
          return EMPTY;
        }

        router.navigate(['/error'], {
          queryParams: { type: 'backend-unavailable', from: req.url },
          skipLocationChange: false
        });
        return EMPTY;
      }

      if (error.status === 401 && !req.url.includes(loginApiUrl)) {
        localStorage.removeItem('token');
        localStorage.removeItem('selectedTripId');
        console.error('Interceptor: Token expirado o inválido. Redirigiendo a /login...');
        router.navigate(['/login']);
        return EMPTY;
      }

      if (error.status === 403 && !req.url.includes(loginApiUrl)) {
        const criticalEndpoints = ['/user/me', '/user/profile', '/user/get'];
        const isCriticalEndpoint = criticalEndpoints.some(endpoint => req.url.includes(endpoint));

        if (isCriticalEndpoint) {
          console.error('Interceptor: Usuario no encontrado o sin permisos (403). Limpiando sesión y redirigiendo a /login...');
          localStorage.removeItem('token');
          localStorage.removeItem('selectedTripId');
          router.navigate(['/login']);
          return EMPTY;
        }
      }

      return throwError(() => error);
    })
  );
};
