import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { Subject, forkJoin, interval, of } from 'rxjs';
import { catchError, takeUntil } from 'rxjs/operators';
import { EnrollmentService } from '../../core/services/enrollment.service';
import { ClaimService } from '../../core/services/claim.service';
import { DisbursementService } from '../../core/services/disbursement.service';
import { RefreshService } from '../../core/services/refresh.service';

const POLL_MS = 30_000;

@Component({
  selector: 'app-officer-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule, RouterModule],
  templateUrl: './officer-dashboard.component.html',
  styleUrl: './officer-dashboard.component.css'
})
export class OfficerDashboardComponent implements OnInit, OnDestroy {
  pendingEnrollments = 0;
  pendingClaims = 0;
  totalEnrollments = 0;
  activeDisbursements = 0;
  summaryCards: any[] = [];
  loading = true;
  lastUpdated: Date | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private enrollmentSvc: EnrollmentService,
    private claimSvc: ClaimService,
    private disbursementSvc: DisbursementService,
    private refresh: RefreshService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.refreshCards();
    this.loadAll();

    // Auto-refresh every 30s while the dashboard is mounted.
    interval(POLL_MS).pipe(takeUntil(this.destroy$)).subscribe(() => this.loadAll(true));

    // Refresh immediately when any other officer action mutates relevant data.
    this.refresh.events$.pipe(takeUntil(this.destroy$)).subscribe(topic => {
      if (topic === 'claims' || topic === 'enrollments' || topic === 'disbursements') {
        this.loadAll(true);
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadAll(silent = false) {
    if (!silent) this.loading = true;
    const empty = { data: [] as any[] };
    forkJoin({
      enrollments: this.enrollmentSvc.getAll().pipe(catchError(() => of(empty))),
      claims: this.claimSvc.getAll().pipe(catchError(() => of(empty))),
      disbursements: this.disbursementSvc.getAll().pipe(catchError(() => of(empty)))
    }).subscribe(({ enrollments, claims, disbursements }) => {
      const eData = enrollments.data ?? [];
      const cData = claims.data ?? [];
      const dData = disbursements.data ?? [];
      this.totalEnrollments = eData.length;
      this.pendingEnrollments = eData.filter((e: any) => e.status === 'PENDING').length;
      this.pendingClaims = cData.filter((c: any) => c.status === 'PENDING').length;
      this.activeDisbursements = dData.filter((d: any) => d.status !== 'COMPLETED' && d.status !== 'FAILED').length;
      this.loading = false;
      this.lastUpdated = new Date();
      this.refreshCards();
      this.cdr.markForCheck();
    });
  }

  refreshCards() {
    this.summaryCards = [
      { label: 'Pending Enrollments', value: this.pendingEnrollments, icon: 'pending_actions', grad: 'linear-gradient(135deg,#f59e0b,#d97706)', glow: '#f59e0b' },
      { label: 'Pending Claims', value: this.pendingClaims, icon: 'receipt_long', grad: 'linear-gradient(135deg,#ef4444,#dc2626)', glow: '#ef4444' },
      { label: 'Total Enrollments', value: this.totalEnrollments, icon: 'assignment', grad: 'linear-gradient(135deg,#6366f1,#4f46e5)', glow: '#6366f1' },
      { label: 'Active Disbursements', value: this.activeDisbursements, icon: 'account_balance_wallet', grad: 'linear-gradient(135deg,#059669,#10b981)', glow: '#10b981' },
    ];
  }
}
