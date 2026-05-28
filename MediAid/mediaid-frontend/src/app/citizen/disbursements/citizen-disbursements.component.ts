import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastrService } from 'ngx-toastr';
import { DisbursementService } from '../../core/services/disbursement.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-citizen-disbursements',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatProgressSpinnerModule, StatusBadgeComponent],
  templateUrl: './citizen-disbursements.component.html',
  styleUrl: './citizen-disbursements.component.css'
})
export class CitizenDisbursementsComponent implements OnInit {
  disbursements: any[] = [];
  loading = true;

  constructor(private disbursementSvc: DisbursementService, private toastr: ToastrService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.disbursementSvc.getMy().subscribe({
      next: r => { this.loading = false; this.disbursements = r.data ?? []; this.cdr.markForCheck(); },
      error: () => { this.loading = false; this.toastr.error('Could not load disbursements.'); this.cdr.markForCheck(); }
    });
  }
}
