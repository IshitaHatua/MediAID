import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { ToastrService } from 'ngx-toastr';
import { ClaimService } from '../../core/services/claim.service';
import { CitizenService } from '../../core/services/citizen.service';
import { RefreshService } from '../../core/services/refresh.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-claim-management',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatIconModule, MatSelectModule, MatFormFieldModule, MatProgressSpinnerModule, MatDialogModule, MatExpansionModule, StatusBadgeComponent],
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

  constructor(
    private claimSvc: ClaimService,
    private citizenSvc: CitizenService,
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

  viewDoc(fileName: string) {
    this.claimSvc.downloadDocument(fileName).subscribe({
      next: blob => {
        const typed = new Blob([blob], { type: this.mimeFor(fileName) });
        const url = URL.createObjectURL(typed);
        const win = window.open(url, '_blank');
        if (!win) this.toastr.warning('Pop-up blocked. Allow pop-ups or use Download.');
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
        this.cdr.markForCheck();
      },
      error: () => { this.toastr.error('Could not load document.'); this.cdr.markForCheck(); }
    });
  }

  downloadDoc(fileName: string) {
    this.claimSvc.downloadDocument(fileName).subscribe({
      next: blob => {
        const typed = new Blob([blob], { type: this.mimeFor(fileName) });
        const url = URL.createObjectURL(typed);
        const a = document.createElement('a');
        a.href = url; a.download = this.originalFileName(fileName); a.click();
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
}
