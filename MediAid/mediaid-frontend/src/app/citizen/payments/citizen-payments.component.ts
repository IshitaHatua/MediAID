import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastrService } from 'ngx-toastr';
import { PaymentService } from '../../core/services/payment.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-citizen-payments',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatProgressSpinnerModule, StatusBadgeComponent],
  templateUrl: './citizen-payments.component.html',
  styleUrl: './citizen-payments.component.css'
})
export class CitizenPaymentsComponent implements OnInit {
  payments: any[] = [];
  loading = true;

  constructor(private paymentSvc: PaymentService, private toastr: ToastrService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.paymentSvc.getMy().subscribe({
      next: r => { this.loading = false; this.payments = r.data ?? []; this.cdr.markForCheck(); },
      error: () => { this.loading = false; this.toastr.error('Could not load payments.'); this.cdr.markForCheck(); }
    });
  }
}
