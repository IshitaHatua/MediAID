import { Component, OnInit, inject } from '@angular/core';
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
import { DisbursementService } from '../../core/services/disbursement.service';
import { PaymentService } from '../../core/services/payment.service';
import { SchemeService } from '../../core/services/scheme.service';
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
  disbursements: any[] = [];
  payments: any[] = [];
  schemes: any[] = [];
  disbLoading = true;
  payLoading = true;
  showDisbForm = false;
  showPayForm = false;
  disbCols = ['disbursementId', 'claimId', 'citizenId', 'amount', 'status'];
  payCols = ['paymentId', 'disbursementId', 'method', 'amount', 'status'];

  private fb = inject(FormBuilder);
  disbForm = this.fb.group({ claimId: [null, Validators.required], citizenId: [null, Validators.required], schemeId: [null, Validators.required], amount: [null, Validators.required] });
  payForm = this.fb.group({ disbursementId: [null, Validators.required], amount: [null, Validators.required], method: ['Bank Transfer', Validators.required] });

  constructor(
    private disbSvc: DisbursementService,
    private paySvc: PaymentService,
    private schemeSvc: SchemeService,
    private refresh: RefreshService,
    private toastr: ToastrService
  ) {}

  ngOnInit() {
    this.disbSvc.getAll().subscribe({
      next: r => { this.disbLoading = false; this.disbursements = r.data ?? []; },
      error: () => { this.disbLoading = false; this.toastr.error('Could not load disbursements.'); }
    });
    this.paySvc.getAll().subscribe({
      next: r => { this.payLoading = false; this.payments = r.data ?? []; },
      error: () => { this.payLoading = false; this.toastr.error('Could not load payments.'); }
    });
    this.schemeSvc.getAll().subscribe({
      next: r => { this.schemes = r.data ?? []; },
      error: () => { this.schemes = []; }
    });
  }

  createDisbursement() {
    if (this.disbForm.invalid) { this.disbForm.markAllAsTouched(); return; }
    const payload = {
      ...this.disbForm.value,
      status: 'Pending',
      date: new Date().toISOString().slice(0, 19)
    };
    this.disbSvc.create(payload as any).subscribe({
      next: r => {
        if (r.data) this.disbursements.unshift(r.data);
        this.showDisbForm = false;
        this.disbForm.reset();
        this.refresh.notify('disbursements');
        this.toastr.success('Disbursement created!');
      },
      error: () => this.toastr.error('Could not create disbursement.')
    });
  }

  createPayment() {
    if (this.payForm.invalid) { this.payForm.markAllAsTouched(); return; }
    const payload = {
      ...this.payForm.value,
      status: 'Pending',
      date: new Date().toISOString().slice(0, 19)
    };
    this.paySvc.create(payload as any).subscribe({
      next: r => {
        if (r.data) this.payments.unshift(r.data);
        this.showPayForm = false;
        this.payForm.reset({ method: 'Bank Transfer' });
        this.refresh.notify('payments');
        this.toastr.success('Payment processed!');
      },
      error: () => this.toastr.error('Could not process payment.')
    });
  }
}
