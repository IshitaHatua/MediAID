import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { AuditLog, AuditManagementLog, FormalAuditRequest, FormalAuditResponse, FormalAuditUpdate } from '../models/audit.models';
import { SKIP_ERROR_TOAST } from '../interceptors/jwt.interceptor';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private base = `${environment.apiUrl}/api/audit`;
  constructor(private http: HttpClient) {}

  getAll() { return this.http.get<ApiResponse<AuditLog[]>>(this.base); }
  getByUser(userId: number) { return this.http.get<ApiResponse<AuditLog[]>>(`${this.base}/${userId}`); }
  getLatest100Logs() {
    return this.http.get<ApiResponse<AuditLog[]>>(`${this.base}/latest`, {
      context: new HttpContext().set(SKIP_ERROR_TOAST, true)
    });
  }
}

@Injectable({ providedIn: 'root' })
export class AuditManagementService {
  private base = `${environment.apiUrl}/api/audit-management`;
  constructor(private http: HttpClient) {}

  getLogs() { return this.http.get<ApiResponse<AuditManagementLog[]>>(`${this.base}/logs`); }
  getLatest100Logs() {
    return this.http.get<ApiResponse<AuditManagementLog[]>>(`${this.base}/logs/latest`, {
      context: new HttpContext().set(SKIP_ERROR_TOAST, true)
    });
  }
  getLogsByUser(userId: number) { return this.http.get<ApiResponse<AuditManagementLog[]>>(`${this.base}/logs/user/${userId}`); }
  getLogsByAction(action: string) { return this.http.get<ApiResponse<AuditManagementLog[]>>(`${this.base}/logs/action/${action}`); }
  getLogsByResource(fragment: string) { return this.http.get<ApiResponse<AuditManagementLog[]>>(`${this.base}/logs/resource/${fragment}`); }
  createAudit(payload: FormalAuditRequest) { return this.http.post<ApiResponse<FormalAuditResponse>>(`${this.base}/audits`, payload); }
  getAllAudits() { return this.http.get<ApiResponse<FormalAuditResponse[]>>(`${this.base}/audits`); }
  getAuditById(id: number) { return this.http.get<ApiResponse<FormalAuditResponse>>(`${this.base}/audits/${id}`); }
  getAuditsByStatus(status: string) { return this.http.get<ApiResponse<FormalAuditResponse[]>>(`${this.base}/audits/status/${status}`); }
  getAuditsByScope(scope: string) { return this.http.get<ApiResponse<FormalAuditResponse[]>>(`${this.base}/audits/scope/${scope}`); }
  updateAudit(id: number, payload: FormalAuditUpdate) { return this.http.patch<ApiResponse<FormalAuditResponse>>(`${this.base}/audits/${id}`, payload); }
  triggerCompliance(auditId: number) { return this.http.post<ApiResponse<FormalAuditResponse>>(`${this.base}/audits/${auditId}/trigger-compliance`, {}); }
}
