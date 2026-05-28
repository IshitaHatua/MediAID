import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { map } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastrService } from 'ngx-toastr';
import { AuditManagementService } from '../../core/services/audit.service';
import { AuthService } from '../../core/services/auth.service';
import { ClaimService } from '../../core/services/claim.service';
import { EnrollmentService } from '../../core/services/enrollment.service';
import { DisbursementService } from '../../core/services/disbursement.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

interface EntityOption { id: number; label: string; status: string; }

@Component({
  selector: 'app-compliance-audits',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    MatButtonModule, MatIconModule, MatTableModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatTabsModule, MatExpansionModule, MatProgressSpinnerModule,
    StatusBadgeComponent
  ],
  templateUrl: './compliance-audits.component.html',
  styleUrl: './compliance-audits.component.css'
})
export class ComplianceAuditsComponent implements OnInit {
  audits: any[] = [];
  filtered: any[] = [];
  logs: any[] = [];
  loading = true;
  logsLoading = true;
  auditAccessDenied = false;
  logAccessDenied = false;
  showCreateForm = false;
  filterStatus = '';
  filterScope = '';
  logCols = ['logId', 'userId', 'action', 'resource', 'timestamp'];

  // ── Officer ID auto-populated from auth (never shown to user) ──
  loggedInOfficerId = 0;

  // ── Audit entity picker state ────────────────────────────────
  auditEntities: EntityOption[] = [];
  auditEntitiesLoading = false;
  auditEntitiesError = false;
  auditSelectedEntity: EntityOption | null = null;

  private fb = inject(FormBuilder);
  auditForm = this.fb.group({
    scope: ['CLAIM', Validators.required],
    findings: ['']
  });

  constructor(
    private auditMgmtSvc: AuditManagementService,
    private authSvc: AuthService,
    private claimSvc: ClaimService,
    private enrollmentSvc: EnrollmentService,
    private disbursementSvc: DisbursementService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loggedInOfficerId = Number(this.authSvc.getUserId());
    this.loadAudits();
    this.loadLogs();
  }

  // Toggle form open/close — auto-fetches entities for default scope on open,
  // resets entity state on close.
  toggleAuditForm() {
    this.showCreateForm = !this.showCreateForm;
    if (this.showCreateForm) {
      this.onAuditScopeChange();
    } else {
      this.auditSelectedEntity = null;
      this.auditEntities = [];
      this.auditEntitiesError = false;
    }
  }

  // Called when scope mat-select changes AND when the form is first opened.
  onAuditScopeChange() {
    this.auditSelectedEntity = null;
    this.auditEntities = [];
    this.auditEntitiesError = false;
    this.auditEntitiesLoading = true;

    const scope = this.auditForm.get('scope')?.value ?? 'CLAIM';
    let obs$;

    if (scope === 'CLAIM') {
      obs$ = this.claimSvc.getAll().pipe(
        map(r => (r.data ?? []).map((c: any): EntityOption => ({
          id: c.claimId,
          label: `Claim #${c.claimId} — ₹${c.claimAmount} — ${c.status}`,
          status: c.status
        })))
      );
    } else if (scope === 'POLICY') {
      obs$ = this.enrollmentSvc.getAll().pipe(
        map(r => (r.data ?? []).map((e: any): EntityOption => ({
          id: e.enrollmentId,
          label: `Enrollment #${e.enrollmentId} — Citizen ${e.citizenId} — expires ${e.expiryDate} — ${e.status}`,
          status: e.status
        })))
      );
    } else {
      obs$ = this.disbursementSvc.getAll().pipe(
        map(r => (r.data ?? []).map((d: any): EntityOption => ({
          id: d.disbursementId,
          label: `Disbursement #${d.disbursementId} — ₹${d.amount} — Claim ${d.claimId} — ${d.status}`,
          status: d.status
        })))
      );
    }

    obs$.subscribe({
      next: entities => {
        this.auditEntitiesLoading = false;
        this.auditEntities = entities;
        this.cdr.markForCheck();
      },
      error: () => {
        this.auditEntitiesLoading = false;
        this.auditEntitiesError = true;
        this.cdr.markForCheck();
      }
    });
  }

  loadAudits() {
    this.loading = true;
    this.auditAccessDenied = false;
    this.auditMgmtSvc.getAllAudits().subscribe({
      next: r => {
        this.loading = false;
        this.audits = (r.data ?? []).map((a: any) => ({ ...a, _newStatus: a.status }));
        this.applyFilter();
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        this.loading = false;
        if (err?.status === 403 || err?.status === 401) {
          this.auditAccessDenied = true;
        } else {
          this.toastr.error('Could not load audits.');
        }
        this.cdr.markForCheck();
      }
    });
  }

  loadLogs() {
    this.logsLoading = true;
    this.logAccessDenied = false;
    this.auditMgmtSvc.getLogs().subscribe({
      next: r => { this.logsLoading = false; this.logs = r.data ?? []; this.cdr.markForCheck(); },
      error: (err: any) => {
        this.logsLoading = false;
        this.logs = [];
        if (err?.status === 403 || err?.status === 401) {
          this.logAccessDenied = true;
        }
        this.cdr.markForCheck();
      }
    });
  }

  applyFilter() {
    this.filtered = this.audits.filter(a =>
      (!this.filterStatus || a.status === this.filterStatus) &&
      (!this.filterScope || a.scope === this.filterScope)
    );
  }

  createAudit() {
    if (!this.auditSelectedEntity) {
      this.toastr.error('Please select a record');
      return;
    }
    if (this.auditForm.invalid) { this.auditForm.markAllAsTouched(); return; }

    const payload = {
      officerId: this.loggedInOfficerId,
      scope: this.auditForm.value.scope,
      scopeEntityId: this.auditSelectedEntity.id,
      findings: this.auditForm.value.findings ?? ''
    };

    this.auditMgmtSvc.createAudit(payload as any).subscribe({
      next: r => {
        if (r.data) this.audits.unshift({ ...r.data, _newStatus: r.data.status });
        this.applyFilter();
        this.showCreateForm = false;
        this.auditForm.reset({ scope: 'CLAIM' });
        this.auditSelectedEntity = null;
        this.auditEntities = [];
        this.toastr.success('Audit created!');
        this.cdr.markForCheck();
      },
      error: () => { this.toastr.error('Could not create audit.'); this.cdr.markForCheck(); }
    });
  }

  updateAudit(a: any) {
    this.auditMgmtSvc.updateAudit(a.auditId, { status: a._newStatus }).subscribe({
      next: r => {
        if (r.data) { a.status = r.data.status; }
        this.applyFilter();
        this.toastr.success('Audit updated.');
        this.cdr.markForCheck();
      },
      error: () => { this.toastr.error('Could not update audit.'); this.cdr.markForCheck(); }
    });
  }

  triggerCompliance(a: any) {
    this.auditMgmtSvc.triggerCompliance(a.auditId).subscribe({
      next: () => { this.toastr.success('Compliance triggered for audit.'); this.cdr.markForCheck(); },
      error: () => { this.toastr.error('Could not trigger compliance.'); this.cdr.markForCheck(); }
    });
  }
}
