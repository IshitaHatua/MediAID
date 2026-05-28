import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { SchemeRequest, SchemeResponse, SchemeStatusUpdate } from '../models/scheme.models';

@Injectable({ providedIn: 'root' })
export class SchemeService {
  private base = `${environment.apiUrl}/api/schemes`;
  constructor(private http: HttpClient) {}

  getAll() { return this.http.get<ApiResponse<SchemeResponse[]>>(this.base); }
  getById(id: number) { return this.http.get<ApiResponse<SchemeResponse>>(`${this.base}/${id}`); }
  create(payload: SchemeRequest) { return this.http.post<ApiResponse<SchemeResponse>>(this.base, payload); }
  updateStatus(id: number, payload: SchemeStatusUpdate) {
    return this.http.patch<ApiResponse<SchemeResponse>>(`${this.base}/${id}/status`, payload);
  }
  delete(id: number) { return this.http.delete<ApiResponse<void>>(`${this.base}/${id}`); }
}
