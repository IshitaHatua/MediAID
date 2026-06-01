import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { ClaimService } from '../../core/services/claim.service';
import { ClaimDocumentResponse } from '../../core/models/claim.models';
import { RefreshService } from '../../core/services/refresh.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-claim-management',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, StatusBadgeComponent],
  templateUrl: './claim-management.component.html',
  styleUrl: './claim-management.component.css'
})
export class ClaimManagementComponent implements OnInit {
  claims: any[] = [];
  filtered: any[] = [];
  filterStatus = '';
  loading = true;
  claimDocs: Record<number, any[]> = {};
  docsLoading: Record<number, boolean> = {};
  expandedClaim: number | null = null;

  constructor(
    private claimSvc: ClaimService,
    private refresh: RefreshService,
    private toastr: ToastrService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.claimSvc.getAll().subscribe({
      next: r => { this.loading = false; if (r.data) { this.claims = r.data; this.applyFilter(); } this.cdr.markForCheck(); },
      error: () => { this.loading = false; this.toastr.error('Could not load claims.'); this.cdr.markForCheck(); }
    });
  }

  applyFilter() {
    this.filtered = this.filterStatus ? this.claims.filter(c => c.status === this.filterStatus) : this.claims;
  }

  toggleClaim(claimId: number) {
    this.expandedClaim = this.expandedClaim === claimId ? null : claimId;
    if (this.expandedClaim === claimId) this.loadClaimDocs(claimId);
  }

  loadClaimDocs(claimId: number) {
    if (this.claimDocs[claimId] || this.docsLoading[claimId]) return;
    this.docsLoading[claimId] = true;
    this.claimSvc.getDocuments(claimId).subscribe({
      next: r => { this.docsLoading[claimId] = false; this.claimDocs[claimId] = r.data ?? []; this.cdr.markForCheck(); },
      error: () => { this.docsLoading[claimId] = false; this.claimDocs[claimId] = []; this.cdr.markForCheck(); }
    });
  }

  originalFileName(name: string): string {
    if (!name) return '';
    const idx = name.indexOf('_');
    return idx >= 0 ? name.substring(idx + 1) : name;
  }

  private safeDownloadName(name: string): string {
    return this.originalFileName(name)
      .replace(/[()\s]+/g, '_')
      .replace(/_+/g, '_')
      .replace(/^_|_$/g, '');
  }

  private mimeFor(name: string): string {
    const ext = (name.split('.').pop() || '').toLowerCase();
    switch (ext) {
      case 'pdf': return 'application/pdf';
      case 'png': return 'image/png';
      case 'jpg':
      case 'jpeg': return 'image/jpeg';
      case 'gif': return 'image/gif';
      default: return 'application/octet-stream';
    }
  }

  viewDoc(doc: ClaimDocumentResponse) {
    this.claimSvc.downloadDocument(doc.documentId).subscribe({
      next: blob => {
        const typed = new Blob([blob], { type: this.mimeFor(doc.fileName) });
        const url = URL.createObjectURL(typed);
        const win = window.open(url, '_blank');
        if (!win) this.toastr.warning('Pop-up blocked. Allow pop-ups or use Download.');
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
        this.cdr.markForCheck();
      },
      error: () => { this.toastr.error('Could not load document.'); this.cdr.markForCheck(); }
    });
  }

  downloadDoc(doc: ClaimDocumentResponse) {
    this.claimSvc.downloadDocument(doc.documentId).subscribe({
      next: blob => {
        const typed = new Blob([blob], { type: this.mimeFor(doc.fileName) });
        const url = URL.createObjectURL(typed);
        const a = document.createElement('a');
        a.href = url; a.download = this.safeDownloadName(doc.fileName); a.click();
        URL.revokeObjectURL(url);
        this.cdr.markForCheck();
      },
      error: () => { this.toastr.error('Download failed.'); this.cdr.markForCheck(); }
    });
  }

  updateStatus(c: any, status: string) {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: { title: `${status} Claim`, message: `${status} claim #${c.claimId}?` }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.claimSvc.updateStatus(c.claimId, { status }).subscribe({
        next: r => {
          if (r.data) c.status = r.data.status;
          this.applyFilter();
          this.refresh.notify('claims');
          this.toastr.success(`Claim ${status.toLowerCase()}.`);
          this.cdr.markForCheck();
        },
        error: () => { this.toastr.error(`Could not ${status.toLowerCase()} claim.`); this.cdr.markForCheck(); }
      });
    });
  }

  generateDisbursement(c: any) {
    this.claimSvc.generateDisbursement(c.claimId).subscribe({
      next: () => {
        this.refresh.notify('disbursements');
        this.toastr.success(`Disbursement generated for claim #${c.claimId}.`);
        this.cdr.markForCheck();
      },
      error: () => {
        this.toastr.error('Could not generate disbursement. Check the disbursement page — one may already exist.');
        this.cdr.markForCheck();
      }
    });
  }
}
