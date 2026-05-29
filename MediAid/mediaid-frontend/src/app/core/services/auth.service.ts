import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, AuthResponse, ForgotPasswordRequest, LoginRequest, RegisterRequest, ResetPasswordRequest } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private base = `${environment.apiUrl}/api/auth`;

  constructor(private http: HttpClient, private router: Router) {}

  login(payload: LoginRequest) {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.base}/login`, payload).pipe(
      tap(res => {
        if (res.data) {
          localStorage.setItem('token', res.data.token);
          localStorage.setItem('role', res.data.role);
          try {
            const decoded: any = JSON.parse(atob(res.data.token.split('.')[1]));
            localStorage.setItem('userId', String(decoded.userId));
            localStorage.setItem('userName', decoded.sub || '');
          } catch {}
        }
      })
    );
  }

  register(payload: RegisterRequest) {
    return this.http.post<ApiResponse<null>>(`${this.base}/register`, payload);
  }

  forgotPassword(payload: ForgotPasswordRequest) {
    return this.http.post<ApiResponse<null>>(`${this.base}/forgot-password`, payload);
  }

  resetPassword(payload: ResetPasswordRequest) {
    return this.http.post<ApiResponse<null>>(`${this.base}/reset-password`, payload);
  }

  logout() {
    // Clear only auth-session keys. Keep app-level keys like `seenUsers` so first-vs-
    // returning-login detection still works after the user logs out and back in.
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');
    localStorage.removeItem('userName');
    this.router.navigate(['/auth/login']);
  }

  /**
   * Records the current user as having logged in on this device.
   * Returns true if the user had logged in before (returning user),
   * false if this is the first time we see them (first-time login).
   */
  markUserSeen(): boolean {
    const userId = this.getUserId();
    if (!userId) return false;
    let seen: string[];
    try {
      seen = JSON.parse(localStorage.getItem('seenUsers') || '[]');
      if (!Array.isArray(seen)) seen = [];
    } catch {
      seen = [];
    }
    const wasSeen = seen.includes(userId);
    if (!wasSeen) {
      seen.push(userId);
      localStorage.setItem('seenUsers', JSON.stringify(seen));
    }
    return wasSeen;
  }

  getToken() { return localStorage.getItem('token'); }
  getRole() { return localStorage.getItem('role'); }
  getUserId() { return localStorage.getItem('userId'); }
  getUserName() { return localStorage.getItem('userName'); }
  isLoggedIn() {
    const token = this.getToken();
    if (!token) return false;
    if (this.isTokenExpired(token)) { localStorage.clear(); return false; }
    return true;
  }

  /** Returns true if the JWT has an `exp` claim that's already past. Malformed tokens are treated as expired. */
  isTokenExpired(token: string): boolean {
    try {
      const payload: any = JSON.parse(atob(token.split('.')[1]));
      if (!payload.exp) return false;
      return Date.now() >= payload.exp * 1000;
    } catch {
      return true;
    }
  }

  getDashboardRoute(): string {
    const routes: Record<string, string> = {
      CITIZEN: '/citizen/dashboard',
      OFFICER: '/officer/dashboard',
      MANAGER: '/manager/dashboard',
      ADMIN: '/admin/dashboard',
      COMPLIANCE: '/compliance/dashboard',
      AUDITOR: '/auditor/dashboard'
    };
    return routes[this.getRole() ?? ''] ?? '/auth/login';
  }
}
