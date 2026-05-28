import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { ComplianceEvaluationRequest, ComplianceRecordRequest, ComplianceRecordResponse } from '../models/compliance.models';

@Injectable({ providedIn: 'root' })
export class ComplianceService {
  private base = `${environment.apiUrl}/api/compliance`;
  constructor(private http: HttpClient) {}

  evaluate(entityId: number, entityType: string, requestedBy: number) {
    return this.http.post<ApiResponse<any>>(`${this.base}/evaluate?entityId=${entityId}&entityType=${entityType}&requestedBy=${requestedBy}`, {});
  }
  evaluateFull(payload: ComplianceEvaluationRequest) {
    return this.http.post<ApiResponse<any>>(`${this.base}/evaluate/full`, payload);
  }
  createRecord(payload: ComplianceRecordRequest) {
    return this.http.post<ApiResponse<ComplianceRecordResponse>>(`${this.base}/records`, payload);
  }
  getAll() { return this.http.get<ApiResponse<ComplianceRecordResponse[]>>(`${this.base}/records`); }
  getById(id: number) { return this.http.get<ApiResponse<ComplianceRecordResponse>>(`${this.base}/records/${id}`); }
  getByEntity(entityId: number) { return this.http.get<ApiResponse<ComplianceRecordResponse[]>>(`${this.base}/records/entity/${entityId}`); }
  getByType(entityType: string) { return this.http.get<ApiResponse<ComplianceRecordResponse[]>>(`${this.base}/records/type/${entityType}`); }
  getViolations() { return this.http.get<ApiResponse<ComplianceRecordResponse[]>>(`${this.base}/records/violations`); }
  getFlagged() { return this.http.get<ApiResponse<ComplianceRecordResponse[]>>(`${this.base}/records/flagged`); }
}
