import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastrService } from 'ngx-toastr';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DisbursementService } from '../../core/services/disbursement.service';
import { PaymentService } from '../../core/services/payment.service';
import { SchemeService } from '../../core/services/scheme.service';
import { CitizenService } from '../../core/services/citizen.service';
import { ClaimService } from '../../core/services/claim.service';
import { RefreshService } from '../../core/services/refresh.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-disbursement-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatIconModule, MatTableModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatTabsModule, MatProgressSpinnerModule, StatusBadgeComponent],
  templateUrl: './disbursement-management.component.html',
  styleUrl: './disbursement-management.component.css'
})
export class DisbursementManagementComponent implements OnInit {
  disbursements: any[] = [];          // filtered: PENDING + PROCESSING only
  payments: any[] = [];
  disbLoading = true;
  payLoading = true;
  showPayForm = false;

  // Lookups for the human-readable disbursement sentence.
  private citizensById: Record<number, any> = {};
  private claimsById:   Record<number, any> = {};
  private schemesById:  Record<number, any> = {};

  disbCols = ['details', 'amount', 'status', 'actions'];
  payCols  = ['paymentId', 'disbursementId', 'method', 'amount', 'status'];

  private fb = inject(FormBuilder);
  payForm = this.fb.group({
    disbursementId: [null, Validators.required],
    amount:         [null, Validators.required],
    method:         ['Bank Transfer', Validators.required]
  });

  constructor(
    private disbSvc: DisbursementService,
    private paySvc: PaymentService,
    private schemeSvc: SchemeService,
    private citizenSvc: CitizenService,
    private claimSvc: ClaimService,
    private refresh: RefreshService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() { this.loadAll(); }

  /**
   * Single forkJoin so the table only renders when every lookup is in.
   * Without this the sentence below would briefly show "Scheme #4" before
   * the schemes call finishes.
   */
  loadAll() {
    this.disbLoading = true;
    this.payLoading = true;
    const empty = { data: [] as any[] };
    forkJoin({
      disbursements: this.disbSvc.getAll().pipe(catchError(() => of(empty))),
      payments:      this.paySvc.getAll().pipe(catchError(() => of(empty))),
      schemes:       this.schemeSvc.getAll().pipe(catchError(() => of(empty))),
      citizens:      this.citizenSvc.getAll().pipe(catchError(() => of(empty))),
      claims:        this.claimSvc.getAll().pipe(catchError(() => of(empty)))
    }).subscribe(({ disbursements, payments, schemes, citizens, claims }) => {
      for (const s of (schemes.data  ?? [])) this.schemesById[s.schemeId]   = s;
      for (const c of (citizens.data ?? [])) this.citizensById[c.citizenId] = c;
      for (const k of (claims.data   ?? [])) this.claimsById[k.claimId]     = k;

      // Only PENDING and PROCESSING are surfaced. COMPLETED / FAILED disappear
      // naturally, satisfying the "should not be shown" rule once auto-complete fires.
      const all = (disbursements.data ?? []) as any[];
      this.disbursements = all.filter(d => {
        const s = (d.status || '').toUpperCase();
        return s === 'PENDING' || s === 'PROCESSING';
      });
      this.payments = (payments.data ?? []);

      this.disbLoading = false;
      this.payLoading = false;
      this.cdr.markForCheck();
    });
  }

  /**
   * "Disbursement of ₹X for <citizen>'s claim — \"<description>\" — under <scheme>."
   * Falls back to ids when any lookup is missing so the cell is never blank.
   */
  describe(d: any): string {
    const citizen   = this.citizensById[d.citizenId];
    const claim     = this.claimsById[d.claimId];
    const scheme    = this.schemesById[d.schemeId];
    const citizenLabel = citizen?.name              ?? `Citizen #${d.citizenId}`;
    const schemeLabel  = scheme?.name               ?? `Scheme #${d.schemeId}`;
    const descLabel    = claim?.description?.trim() ?? `Claim #${d.claimId}`;
    return `Disbursement for ${citizenLabel}'s claim — "${descLabel}" — under ${schemeLabel}.`;
  }

  /** PENDING → PROCESSING. Officer-side "I've started processing this" action. */
  processDisbursement(d: any) {
    this.disbSvc.updateStatus(d.disbursementId, 'PROCESSING').subscribe({
      next: r => {
        if (r.data) d.status = r.data.status;
        this.refresh.notify('disbursements');
        this.toastr.success('Disbursement is now processing.');
        this.cdr.markForCheck();
      },
      error: () => this.toastr.error('Could not start processing this disbursement.')
    });
  }

  /** Officer records a real payment. Status is "Completed" so the payment-service
   *  auto-complete logic (sumCompletedPaymentsByDisbursementId) actually fires. */
  createPayment() {
    if (this.payForm.invalid) { this.payForm.markAllAsTouched(); return; }
    const payload = {
      ...this.payForm.value,
      status: 'Completed',
      date: new Date().toISOString().slice(0, 19)
    };
    this.paySvc.create(payload as any).subscribe({
      next: r => {
        if (r.data) this.payments.unshift(r.data);
        this.showPayForm = false;
        this.payForm.reset({ method: 'Bank Transfer' });
        this.refresh.notify('payments');
        // A payment may have just pushed total-paid >= amount, in which case
        // payment-service auto-flipped the disbursement to COMPLETED. Reload so
        // it drops out of the list.
        this.loadAll();
        this.toastr.success('Payment recorded.');
      },
      error: () => this.toastr.error('Could not process payment.')
    });
  }
}
