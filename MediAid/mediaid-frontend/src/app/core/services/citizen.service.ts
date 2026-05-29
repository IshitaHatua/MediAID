import { Injectable } from '@angular/core';
import { HttpClient, HttpContext, HttpEvent, HttpRequest } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { CitizenDocumentResponse, CitizenRequest, CitizenResponse } from '../models/citizen.models';
import { SKIP_ERROR_TOAST } from '../interceptors/jwt.interceptor';

@Injectable({ providedIn: 'root' })
export class CitizenService {
  private base = `${environment.apiUrl}/api`;
  constructor(private http: HttpClient) {}

  createCitizen(payload: CitizenRequest) {
    return this.http.post<ApiResponse<CitizenResponse>>(`${this.base}/citizens`, payload);
  }
  /**
   * Fetches the citizen profile. Returns 404 with "Citizen Not Found" when the
   * citizen hasn't created their profile yet — this is a normal first-login state,
   * not a real error. Suppress the global toast; callers display a "Profile Required"
   * banner from their .error callback instead.
   */
  getCitizen(citizenId: number) {
    return this.http.get<ApiResponse<CitizenResponse>>(`${this.base}/citizens/${citizenId}`,
      { context: new HttpContext().set(SKIP_ERROR_TOAST, true) });
  }
  getAll() {
    return this.http.get<ApiResponse<CitizenResponse[]>>(`${this.base}/citizens`);
  }
  updateCitizen(citizenId: number, payload: CitizenRequest) {
    return this.http.put<ApiResponse<CitizenResponse>>(`${this.base}/citizens/${citizenId}`, payload);
  }
  verifyCitizen(citizenId: number, status: string) {
    return this.http.put<ApiResponse<CitizenResponse>>(`${this.base}/citizens/${citizenId}/verify?status=${status}`, {});
  }
  suspendCitizen(citizenId: number) {
    return this.http.put<ApiResponse<CitizenResponse>>(`${this.base}/citizens/${citizenId}/suspend`, {});
  }
  uploadDocument(citizenId: number, file: File, docType: string) {
    const fd = new FormData();
    fd.append('file', file);
    fd.append('docType', docType);
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    fd.append('uploadedDate', `${dd}-${mm}-${yyyy}`);
    return this.http.post<ApiResponse<CitizenDocumentResponse>>(`${this.base}/citizens/${citizenId}/documents`, fd);
  }
  uploadDocumentWithProgress(citizenId: number, file: File, docType: string): import('rxjs').Observable<HttpEvent<ApiResponse<CitizenDocumentResponse>>> {
    const fd = new FormData();
    fd.append('file', file);
    fd.append('docType', docType);
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    fd.append('uploadedDate', `${dd}-${mm}-${yyyy}`);
    const req = new HttpRequest('POST', `${this.base}/citizens/${citizenId}/documents`, fd, {
      reportProgress: true
    });
    return this.http.request<ApiResponse<CitizenDocumentResponse>>(req);
  }
  getDocuments(citizenId: number) {
    return this.http.get<ApiResponse<CitizenDocumentResponse[]>>(`${this.base}/citizens/${citizenId}/documents`);
  }
  verifyDocument(documentId: number, status: string) {
    return this.http.put<ApiResponse<CitizenDocumentResponse>>(`${this.base}/documents/${documentId}/verify?status=${status}`, {});
  }
  deleteDocument(documentId: number) {
    return this.http.delete<ApiResponse<void>>(`${this.base}/documents/${documentId}`);
  }
  downloadDocument(fileName: string) {
    // Encode the filename so spaces, parentheses and other URI-unsafe characters
    // (the stored name is `<uuid>_<original>`, and originals like "email (1).pdf"
    // contain spaces and parens) round-trip cleanly through the gateway and
    // Spring's @PathVariable. Without this the backend resolves a different path
    // and returns an error JSON which then gets saved as a corrupted .pdf.
    return this.http.get(`${this.base}/documents/${encodeURIComponent(fileName)}/download`,
      { responseType: 'blob' });
  }
}
