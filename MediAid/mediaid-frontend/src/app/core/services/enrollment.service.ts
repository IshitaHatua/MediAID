import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { EnrollmentRequest, EnrollmentResponse } from '../models/enrollment.models';

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private base = `${environment.apiUrl}/api/enrollments`;
  constructor(private http: HttpClient) {}

  enroll(payload: EnrollmentRequest) { return this.http.post<ApiResponse<EnrollmentResponse>>(this.base, payload); }
  getById(id: number) { return this.http.get<ApiResponse<EnrollmentResponse>>(`${this.base}/${id}`); }
  getMy() { return this.http.get<ApiResponse<EnrollmentResponse[]>>(this.base); }
  getAll() { return this.http.get<ApiResponse<EnrollmentResponse[]>>(`${this.base}/all`); }
  updateStatus(id: number, status: string) {
    return this.http.patch<ApiResponse<EnrollmentResponse>>(`${this.base}/${id}/status?status=${status}`, {});
  }
}
