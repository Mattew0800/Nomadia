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

      // Errores de conexión: backend no disponible
      // status 0 = no se pudo conectar al servidor
      // 502 = Bad Gateway
      // 503 = Service Unavailable
      // 504 = Gateway Timeout
      if (error.status === 0 || error.status === 502 || error.status === 503 || error.status === 504) {
        console.error('Interceptor: Backend no disponible. Redirigiendo a página de error...');
        backendStatus.setBackendAvailable(false);
        router.navigate(['/error'], {
          queryParams: { type: 'backend-unavailable' },
          skipLocationChange: false
        });
        return EMPTY;
      }

      // Token expirado o inválido
      if (error.status === 401 && !req.url.includes(loginApiUrl)) {
        localStorage.removeItem('token');
        console.error('Interceptor: Token expirado o inválido. Redirigiendo a /login...');
        router.navigate(['/login']);
        return EMPTY;
      }

      return throwError(() => error);
    })
  );
};
