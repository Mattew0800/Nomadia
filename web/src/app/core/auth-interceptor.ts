import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
// 1. Importa EMPTY
import { catchError, throwError, EMPTY } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const router = inject(Router);

  return next(req).pipe(

    catchError((error: HttpErrorResponse) => {
      const loginApiUrl = '/login';

      if (error.status === 401 && !req.url.includes(loginApiUrl)) {
        localStorage.removeItem('token');
        console.error('Interceptor: Token expirado o inválido. Redirigiendo a /login...');
        router.navigate(['/login']);

        //evita que se propague el error
        return EMPTY;
      }

      return throwError(() => error);
    })
  );
};
