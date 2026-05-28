import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { PaymentRequest, PaymentResponse } from '../models/payment.models';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private base = `${environment.apiUrl}/api/payment`;
  constructor(private http: HttpClient) {}

  create(payload: PaymentRequest) { return this.http.post<ApiResponse<PaymentResponse>>(this.base, payload); }
  getMy() { return this.http.get<ApiResponse<PaymentResponse[]>>(`${this.base}/my`); }
  getAll() { return this.http.get<ApiResponse<PaymentResponse[]>>(`${this.base}/all`); }
  getByDisbursement(id: number) { return this.http.get<ApiResponse<PaymentResponse>>(`${this.base}/disbursement/${id}`); }
  getById(id: number) { return this.http.get<ApiResponse<PaymentResponse>>(`${this.base}/${id}`); }
}
