import { Component, OnInit, ChangeDetectorRef } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { CitizenService } from '../../core/services/citizen.service';
import { RefreshService } from '../../core/services/refresh.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-citizen-management',
  standalone: true,
  imports: [FormsModule, StatusBadgeComponent],
  templateUrl: './citizen-management.component.html',
  styleUrl: './citizen-management.component.css',
})
export class CitizenManagementComponent implements OnInit {
  pendingCitizens: any[] = [];
  pendingLoading = true;

  expandedCitizen: number | null = null;
  citizenDocs: Record<number, any[]> = {};
  docsLoading: Record<number, boolean> = {};

  constructor(
    private citizenSvc: CitizenService,
    private refresh: RefreshService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.loadPending();
  }

  loadPending() {
    this.pendingLoading = true;
    this.pendingCitizens = [];
    this.expandedCitizen = null;
    this.citizenSvc.getAll().subscribe({
      next: (r) => {
        this.pendingLoading = false;
        if (r.data) this.pendingCitizens = r.data.filter((c: any) => c.status === 'PENDING');
        this.cdr.markForCheck();
      },
      error: () => {
        this.pendingLoading = false;
        this.toastr.error(
          'Could not load pending citizens. Restart the citizen-service and click Refresh.',
        );
        this.cdr.markForCheck();
      },
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
    this.cdr.markForCheck();
    this.citizenSvc.getDocuments(citizenId).subscribe({
      next: (r) => {
        this.docsLoading[citizenId] = false;
        this.citizenDocs[citizenId] = r.data ?? [];
        this.cdr.markForCheck();
      },
      error: () => {
        this.docsLoading[citizenId] = false;
        this.citizenDocs[citizenId] = [];
        this.cdr.markForCheck();
      },
    });
  }

  originalFileName(fileUri: string): string {
    if (!fileUri) return '';
    const idx = fileUri.indexOf('_');
    return idx >= 0 ? fileUri.substring(idx + 1) : fileUri;
  }

  private safeDownloadName(fileUri: string): string {
    return this.originalFileName(fileUri)
      .replace(/[()\s]+/g, '_')
      .replace(/_+/g, '_')
      .replace(/^_|_$/g, '');
  }

  private mimeFor(fileUri: string): string {
    const ext = (fileUri.split('.').pop() || '').toLowerCase();
    switch (ext) {
      case 'pdf':
        return 'application/pdf';
      case 'png':
        return 'image/png';
      case 'jpg':
      case 'jpeg':
        return 'image/jpeg';
      case 'gif':
        return 'image/gif';
      default:
        return 'application/octet-stream';
    }
  }

  viewDoc(fileUri: string) {
    this.citizenSvc.downloadDocument(fileUri).subscribe({
      next: (blob) => {
        const typed = new Blob([blob], { type: this.mimeFor(fileUri) });
        const url = URL.createObjectURL(typed);
        const win = window.open(url, '_blank');
        if (!win) this.toastr.warning('Pop-up blocked. Allow pop-ups or use Download.');
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
      },
      error: () => this.toastr.error('Could not load document. Please try again.'),
    });
  }

  downloadDoc(fileUri: string) {
    this.citizenSvc.downloadDocument(fileUri).subscribe({
      next: (blob) => {
        const typed = new Blob([blob], { type: this.mimeFor(fileUri) });
        const url = URL.createObjectURL(typed);
        const a = document.createElement('a');
        a.href = url;
        a.download = this.safeDownloadName(fileUri);
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.toastr.error('Download failed. Please try again.'),
    });
  }

  verifyFromList(c: any, status: string) {
    this.citizenSvc.verifyCitizen(c.citizenId, status).subscribe({
      next: (r) => {
        if (r.data) c.status = r.data.status;
        this.pendingCitizens = this.pendingCitizens.filter((x) => x.citizenId !== c.citizenId);
        if (this.expandedCitizen === c.citizenId) this.expandedCitizen = null;
        this.refresh.notify('citizens');
        this.toastr.success(`Citizen ${status.toLowerCase()}.`);
        this.cdr.markForCheck();
      },
      error: () => {
        this.toastr.error(`Could not ${status.toLowerCase()} citizen.`);
        this.cdr.markForCheck();
      },
    });
  }

}
