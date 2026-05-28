import { HttpInterceptorFn, HttpErrorResponse, HttpContextToken } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

/**
 * Set this token to true on requests that handle their own errors silently.
 * The interceptor will skip the global toast and just re-throw the error
 * so the caller's catchError can handle it without a duplicate notification.
 *
 * Usage:
 *   import { HttpContext } from '@angular/common/http';
 *   import { SKIP_ERROR_TOAST } from '../interceptors/jwt.interceptor';
 *   this.http.get(url, { context: new HttpContext().set(SKIP_ERROR_TOAST, true) });
 */
export const SKIP_ERROR_TOAST = new HttpContextToken<boolean>(() => false);

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  const router = inject(Router);
  const toastr = inject(ToastrService);

  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      // Caller opted-out of the global error toast — just propagate the error.
      if (req.context.get(SKIP_ERROR_TOAST)) {
        return throwError(() => err);
      }

      if (err.status === 401) {
        localStorage.clear();
        router.navigate(['/auth/login']);
        toastr.error('Session expired. Please login again.');
      } else if (err.status === 403) {
        toastr.error('Access denied.');
      } else if (err.status === 0) {
        toastr.error('Cannot reach the server. Check your connection.');
      } else {
        const msg = err.error?.message || err.message || 'An error occurred.';
        toastr.error(msg);
      }
      return throwError(() => err);
    })
  );
};
