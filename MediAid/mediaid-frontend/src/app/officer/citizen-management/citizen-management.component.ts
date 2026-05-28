import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastrService } from 'ngx-toastr';
import { CitizenService } from '../../core/services/citizen.service';
import { RefreshService } from '../../core/services/refresh.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-citizen-management',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatIconModule, MatTableModule, MatProgressSpinnerModule, StatusBadgeComponent],
  templateUrl: './citizen-management.component.html',
  styleUrl: './citizen-management.component.css'
})
export class CitizenManagementComponent implements OnInit {
  pendingCitizens: any[] = [];
  pendingLoading = true;

  expandedCitizen: number | null = null;
  citizenDocs: Record<number, any[]> = {};
  docsLoading: Record<number, boolean> = {};

  docCols = ['docType', 'fileUri', 'uploadedDate', 'verificationStatus', 'actions'];

  constructor(private citizenSvc: CitizenService, private refresh: RefreshService, private toastr: ToastrService) {}

  ngOnInit() { this.loadPending(); }

  loadPending() {
    this.pendingLoading = true;
    this.pendingCitizens = [];
    this.expandedCitizen = null;
    this.citizenSvc.getAll().subscribe({
      next: r => {
        this.pendingLoading = false;
        if (r.data) this.pendingCitizens = r.data.filter((c: any) => c.status === 'PENDING');
      },
      error: () => {
        this.pendingLoading = false;
        this.toastr.error('Could not load pending citizens. Restart the citizen-service and click Refresh.');
      }
    });
  }

  toggleDocs(citizenId: number) {
    if (this.expandedCitizen === citizenId) {
      this.expandedCitizen = null;
      return;
    }
    this.expandedCitizen = citizenId;
    if (this.citizenDocs[citizenId]) return;
    this.docsLoading[citizenId] = true;
    this.citizenSvc.getDocuments(citizenId).subscribe({
      next: r => { this.docsLoading[citizenId] = false; this.citizenDocs[citizenId] = r.data ?? []; },
      error: () => { this.docsLoading[citizenId] = false; this.citizenDocs[citizenId] = []; }
    });
  }

  originalFileName(fileUri: string): string {
    if (!fileUri) return '';
    const idx = fileUri.indexOf('_');
    return idx >= 0 ? fileUri.substring(idx + 1) : fileUri;
  }

  private mimeFor(fileUri: string): string {
    const ext = (fileUri.split('.').pop() || '').toLowerCase();
    switch (ext) {
      case 'pdf': return 'application/pdf';
      case 'png': return 'image/png';
      case 'jpg':
      case 'jpeg': return 'image/jpeg';
      case 'gif': return 'image/gif';
      default: return 'application/octet-stream';
    }
  }

  viewDoc(fileUri: string) {
    this.citizenSvc.downloadDocument(fileUri).subscribe({
      next: blob => {
        const typed = new Blob([blob], { type: this.mimeFor(fileUri) });
        const url = URL.createObjectURL(typed);
        const win = window.open(url, '_blank');
        if (!win) this.toastr.warning('Pop-up blocked. Allow pop-ups or use Download.');
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
      },
      error: () => this.toastr.error('Could not load document. Please try again.')
    });
  }

  downloadDoc(fileUri: string) {
    this.citizenSvc.downloadDocument(fileUri).subscribe({
      next: blob => {
        const typed = new Blob([blob], { type: this.mimeFor(fileUri) });
        const url = URL.createObjectURL(typed);
        const a = document.createElement('a');
        a.href = url; a.download = this.originalFileName(fileUri); a.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.toastr.error('Download failed. Please try again.')
    });
  }

  verifyFromList(c: any, status: string) {
    this.citizenSvc.verifyCitizen(c.citizenId, status).subscribe({
      next: r => {
        if (r.data) c.status = r.data.status;
        this.pendingCitizens = this.pendingCitizens.filter(x => x.citizenId !== c.citizenId);
        if (this.expandedCitizen === c.citizenId) this.expandedCitizen = null;
        this.refresh.notify('citizens');
        this.toastr.success(`Citizen ${status.toLowerCase()}.`);
      },
      error: () => this.toastr.error(`Could not ${status.toLowerCase()} citizen.`)
    });
  }

  verifyDoc(citizenId: number, documentId: number, status: string) {
    this.citizenSvc.verifyDocument(documentId, status).subscribe({
      next: () => {
        const docs = this.citizenDocs[citizenId];
        if (docs) {
          const target = docs.find(d => d.documentId === documentId);
          if (target) target.verificationStatus = status.toUpperCase();
          this.citizenDocs[citizenId] = [...docs];
        }
        this.toastr.success(`Document ${status.toLowerCase()}.`);
      },
      error: () => this.toastr.error(`Could not ${status.toLowerCase()} document.`)
    });
  }
}
