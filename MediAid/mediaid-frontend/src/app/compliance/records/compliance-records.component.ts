import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { map } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ToastrService } from 'ngx-toastr';
import { ComplianceService } from '../../core/services/compliance.service';
import { ClaimService } from '../../core/services/claim.service';
import { EnrollmentService } from '../../core/services/enrollment.service';
import { DisbursementService } from '../../core/services/disbursement.service';
import { AuthService } from '../../core/services/auth.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

interface EntityOption { id: number; label: string; status: string; }

@Component({
  selector: 'app-compliance-records',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatButtonModule, MatIconModule, MatTableModule,
    MatTabsModule, MatProgressSpinnerModule, MatTooltipModule,
    StatusBadgeComponent
  ],
  templateUrl: './compliance-records.component.html',
  styleUrl: './compliance-records.component.css'
})
export class ComplianceRecordsComponent implements OnInit {

  // ── Records tables ──────────────────────────────────────────
  all: any[] = [];
  violations: any[] = [];
  flagged: any[] = [];
  loading = true;
  cols = ['complianceId', 'entityId', 'entityType', 'result', 'notes', 'evaluatedAt'];

  // ── Full Evaluation picker state ─────────────────────────────
  evalEntityType = '';
  evalEntities: EntityOption[] = [];
  evalLoadingEntities = false;
  evalLoadError = false;
  evalSelected: EntityOption | null = null;
  evalRunning = false;
  evalResult: any = null;

  // ── Create Record picker state ───────────────────────────────
  createEntityType = '';
  createEntities: EntityOption[] = [];
  createLoadingEntities = false;
  createLoadError = false;
  createSelected: EntityOption | null = null;
  createResult = 'PASS';
  createNotes = '';
  createSaving = false;

  constructor(
    private complianceSvc: ComplianceService,
    private claimSvc: ClaimService,
    private enrollmentSvc: EnrollmentService,
    private disbursementSvc: DisbursementService,
    private authSvc: AuthService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() { this.loadAll(); }

  // ── Load compliance tables ────────────────────────────────────
  loadAll() {
    this.loading = true;
    this.complianceSvc.getAll().subscribe({
      next: r => { this.loading = false; this.all = r.data ?? []; this.cdr.markForCheck(); },
      error: () => { this.loading = false; this.toastr.error('Could not load compliance records.'); this.cdr.markForCheck(); }
    });
    this.complianceSvc.getViolations().subscribe({
      next: r => { this.violations = r.data ?? []; this.cdr.markForCheck(); },
      error: () => { this.violations = []; this.cdr.markForCheck(); }
    });
    this.complianceSvc.getFlagged().subscribe({
      next: r => { this.flagged = r.data ?? []; this.cdr.markForCheck(); },
      error: () => { this.flagged = []; this.cdr.markForCheck(); }
    });
  }

  // ── Entity type selection ────────────────────────────────────
  selectEvalType(type: string) {
    this.evalEntityType = type;
    this.evalSelected = null;
    this.evalResult = null;
    this.evalLoadError = false;
    this.fetchEntities(type, 'eval');
  }

  selectCreateType(type: string) {
    this.createEntityType = type;
    this.createSelected = null;
    this.createLoadError = false;
    this.fetchEntities(type, 'create');
  }

  private fetchEntities(type: string, target: 'eval' | 'create') {
    const isEval = target === 'eval';
    if (isEval) { this.evalLoadingEntities = true; this.evalEntities = []; }
    else        { this.createLoadingEntities = true; this.createEntities = []; }

    let obs$;
    if (type === 'CLAIM') {
      obs$ = this.claimSvc.getAll().pipe(
        map(r => (r.data ?? []).map((c: any): EntityOption => ({
          id: c.claimId,
          label: `Claim #${c.claimId} — ₹${c.claimAmount} — ${c.status}`,
          status: c.status
        })))
      );
    } else if (type === 'POLICY') {
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
        if (isEval) { this.evalLoadingEntities = false; this.evalEntities = entities; }
        else        { this.createLoadingEntities = false; this.createEntities = entities; }
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        console.error(`[ComplianceRecords] fetchEntities(${type}, ${target}) failed`, err);
        if (isEval) { this.evalLoadingEntities = false; this.evalLoadError = true; }
        else        { this.createLoadingEntities = false; this.createLoadError = true; }
        this.cdr.markForCheck();
      }
    });
  }

  // ── Retry entity fetch ───────────────────────────────────────
  retryFetch(target: 'eval' | 'create') {
    const type = target === 'eval' ? this.evalEntityType : this.createEntityType;
    if (!type) return;
    this.fetchEntities(type, target);
  }

  // ── Run evaluation ───────────────────────────────────────────
  runEvaluation() {
    if (!this.evalSelected) return;
    this.evalRunning = true;
    this.evalResult = null;
    const userId = Number(this.authSvc.getUserId());
    this.complianceSvc.evaluate(this.evalSelected.id, this.evalEntityType, userId).subscribe({
      next: r => {
        this.evalRunning = false;
        this.evalResult = r.data;
        this.toastr.success('Compliance evaluation complete.');
        this.loadAll();
        this.cdr.markForCheck();
      },
      error: () => {
        this.evalRunning = false;
        this.toastr.error('Evaluation failed. Please try again.');
        this.cdr.markForCheck();
      }
    });
  }

  // ── Save compliance record ───────────────────────────────────
  saveRecord() {
    if (!this.createSelected) return;
    this.createSaving = true;
    const payload = {
      entityId: this.createSelected.id,
      entityType: this.createEntityType,
      result: this.createResult,
      notes: this.createNotes,
      requestedBy: Number(this.authSvc.getUserId())
    };
    this.complianceSvc.createRecord(payload as any).subscribe({
      next: () => {
        this.createSaving = false;
        this.toastr.success('Compliance record saved.');
        this.createSelected = null;
        this.createEntityType = '';
        this.createEntities = [];
        this.createLoadError = false;
        this.createResult = 'PASS';
        this.createNotes = '';
        this.loadAll();
        this.cdr.markForCheck();
      },
      error: () => {
        this.createSaving = false;
        this.toastr.error('Could not save record.');
        this.cdr.markForCheck();
      }
    });
  }
}
