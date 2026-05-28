import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { UserResponse } from '../models/audit.models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private base = `${environment.apiUrl}/api/users`;
  private authBase = `${environment.apiUrl}/api/auth`;
  constructor(private http: HttpClient) {}

  /**
   * Lists every registered user. Sourced from auth-service (the source of truth for registration)
   * so the list reflects the actual users — user-service may point at a separate database.
   */
  getAll() { return this.http.get<ApiResponse<UserResponse[]>>(`${this.authBase}/users`); }

  getById(id: number) { return this.http.get<ApiResponse<UserResponse>>(`${this.base}/${id}`); }
  update(id: number, payload: Partial<UserResponse>) { return this.http.put<ApiResponse<UserResponse>>(`${this.base}/${id}`, payload); }
  delete(id: number) { return this.http.delete<ApiResponse<null>>(`${this.base}/${id}`); }
  updateRole(id: number, role: string) { return this.http.put<ApiResponse<UserResponse>>(`${this.base}/${id}/role`, { role }); }
}
