import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { ClaimDocumentResponse, ClaimRequest, ClaimResponse, ClaimStatusUpdate } from '../models/claim.models';

@Injectable({ providedIn: 'root' })
export class ClaimService {
  private base = `${environment.apiUrl}/api/claims`;
  constructor(private http: HttpClient) {}

  create(payload: ClaimRequest) { return this.http.post<ApiResponse<ClaimResponse>>(this.base, payload); }
  getById(id: number) { return this.http.get<ApiResponse<ClaimResponse>>(`${this.base}/${id}`); }
  getMy() { return this.http.get<ApiResponse<ClaimResponse[]>>(this.base); }
  getAll() { return this.http.get<ApiResponse<ClaimResponse[]>>(`${this.base}/all`); }
  updateStatus(id: number, payload: ClaimStatusUpdate) {
    return this.http.patch<ApiResponse<ClaimResponse>>(`${this.base}/${id}/status`, payload);
  }
  uploadDocument(claimId: number, file: File) {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<ApiResponse<ClaimDocumentResponse>>(`${this.base}/${claimId}/documents`, fd);
  }
  getDocuments(claimId: number) {
    return this.http.get<ApiResponse<ClaimDocumentResponse[]>>(`${this.base}/${claimId}/documents`);
  }
  downloadDocument(fileName: string) {
    return this.http.get(
      `${this.base}/documents/${fileName}/download`,
      { responseType: 'blob' }
    );
  }
  getValidations() { return this.http.get<ApiResponse<any[]>>(`${this.base}/validations`); }
}
