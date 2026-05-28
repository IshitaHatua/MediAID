import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { Subject, forkJoin, of } from 'rxjs';
import { catchError, takeUntil } from 'rxjs/operators';
import { EnrollmentService } from '../../core/services/enrollment.service';
import { ClaimService } from '../../core/services/claim.service';
import { DisbursementService } from '../../core/services/disbursement.service';
import { PaymentService } from '../../core/services/payment.service';
import { SchemeService } from '../../core/services/scheme.service';
import { RefreshService } from '../../core/services/refresh.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-citizen-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule, RouterModule, StatusBadgeComponent],
  templateUrl: './citizen-dashboard.component.html',
  styleUrl: './citizen-dashboard.component.css'
})
export class CitizenDashboardComponent implements OnInit, OnDestroy {
  enrollments: any[] = [];
  claims: any[] = [];
  payments: any[] = [];
  schemes: any[] = [];
  pendingClaims = 0;
  totalDisbursed = 0;
  summaryCards: any[] = [];
  loading = true;

  private destroy$ = new Subject<void>();

  constructor(
    private enrollmentSvc: EnrollmentService,
    private claimSvc: ClaimService,
    private disbursementSvc: DisbursementService,
    private paymentSvc: PaymentService,
    private schemeSvc: SchemeService,
    private refresh: RefreshService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.refreshCards();
    this.loadAll();
    this.refresh.events$.pipe(takeUntil(this.destroy$)).subscribe(topic => {
      if (topic === 'claims' || topic === 'enrollments' || topic === 'disbursements' || topic === 'payments') {
        this.loadAll();
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadAll() {
    this.loading = true;
    const empty = { data: [] as any[] };
    forkJoin({
      enrollments: this.enrollmentSvc.getMy().pipe(catchError(() => of(empty))),
      claims: this.claimSvc.getMy().pipe(catchError(() => of(empty))),
      disbursements: this.disbursementSvc.getMy().pipe(catchError(() => of(empty))),
      payments: this.paymentSvc.getMy().pipe(catchError(() => of(empty))),
      schemes: this.schemeSvc.getAll().pipe(catchError(() => of(empty)))
    }).subscribe(({ enrollments, claims, disbursements, payments, schemes }) => {
      this.enrollments = enrollments.data ?? [];
      this.claims = claims.data ?? [];
      this.payments = payments.data ?? [];
      this.schemes = schemes.data ?? [];
      this.pendingClaims = this.claims.filter((c: any) => c.status === 'PENDING').length;
      this.totalDisbursed = (disbursements.data ?? []).reduce((s: number, d: any) => s + (d.amount || 0), 0);
      this.loading = false;
      this.refreshCards();
      this.cdr.markForCheck();
    });
  }

  schemeName(id: number) { return this.schemes.find(s => s.schemeId === id)?.name || `Scheme #${id}`; }

  refreshCards() {
    const fmt = (n: number) => n >= 100000
      ? '₹' + (n / 100000).toFixed(1) + 'L'
      : n >= 1000
      ? '₹' + (n / 1000).toFixed(1) + 'K'
      : '₹' + n;

    this.summaryCards = [
      { label: 'Total Enrollments', value: this.enrollments.length, icon: 'assignment', grad: 'linear-gradient(135deg,#6366f1,#4f46e5)', glow: '#6366f1' },
      { label: 'Pending Claims', value: this.pendingClaims, icon: 'hourglass_top', grad: 'linear-gradient(135deg,#f59e0b,#d97706)', glow: '#f59e0b' },
      { label: 'Total Disbursed', value: this.totalDisbursed > 0 ? fmt(this.totalDisbursed) : '₹0', icon: 'account_balance_wallet', grad: 'linear-gradient(135deg,#059669,#10b981)', glow: '#10b981' },
      { label: 'Payments Received', value: this.payments.length, icon: 'payments', grad: 'linear-gradient(135deg,#8b5cf6,#7c3aed)', glow: '#8b5cf6' },
    ];
  }
}
