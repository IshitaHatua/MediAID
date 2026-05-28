import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.models';
import { CitizenDocumentResponse, CitizenRequest, CitizenResponse } from '../models/citizen.models';

@Injectable({ providedIn: 'root' })
export class CitizenService {
  private base = `${environment.apiUrl}/api`;
  constructor(private http: HttpClient) {}

  createCitizen(payload: CitizenRequest) {
    return this.http.post<ApiResponse<CitizenResponse>>(`${this.base}/citizens`, payload);
  }
  getCitizen(citizenId: number) {
    return this.http.get<ApiResponse<CitizenResponse>>(`${this.base}/citizens/${citizenId}`);
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
    return this.http.get(`${this.base}/documents/${fileName}/download`, { responseType: 'blob' });
  }
}
