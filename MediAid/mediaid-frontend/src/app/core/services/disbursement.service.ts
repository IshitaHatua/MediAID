import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { DisbursementRequest, DisbursementResponse } from '../models/disbursement.models';

@Injectable({ providedIn: 'root' })
export class DisbursementService {
  private base = `${environment.apiUrl}/api/disbursement`;
  constructor(private http: HttpClient) {}

  create(payload: DisbursementRequest) { return this.http.post<ApiResponse<DisbursementResponse>>(this.base, payload); }
  getById(id: number) { return this.http.get<ApiResponse<DisbursementResponse>>(`${this.base}/${id}`); }
  getByClaimId(claimId: number) { return this.http.get<ApiResponse<DisbursementResponse>>(`${this.base}/claim/${claimId}`); }
  getMy() { return this.http.get<ApiResponse<DisbursementResponse[]>>(`${this.base}/my`); }
  getAll() { return this.http.get<ApiResponse<DisbursementResponse[]>>(`${this.base}/all`); }
  updateStatus(id: number, status: string) {
    return this.http.patch<ApiResponse<DisbursementResponse>>(`${this.base}/${id}/status?status=${status}`, {});
  }
}
