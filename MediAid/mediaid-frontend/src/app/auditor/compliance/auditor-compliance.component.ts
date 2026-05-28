import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { ToastrService } from 'ngx-toastr';
import { ComplianceService } from '../../core/services/compliance.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-auditor-compliance',
  standalone: true,
  imports: [
    CommonModule,
    MatProgressSpinnerModule, MatTableModule,
    MatIconModule, MatTabsModule,
    StatusBadgeComponent
  ],
  templateUrl: './auditor-compliance.component.html',
  styleUrl: './auditor-compliance.component.css'
})
export class AuditorComplianceComponent implements OnInit {
  all: any[] = [];
  violations: any[] = [];
  flagged: any[] = [];
  loading = true;
  cols = ['complianceId', 'entityId', 'entityType', 'result', 'notes', 'evaluatedAt'];

  constructor(private complianceSvc: ComplianceService, private toastr: ToastrService, private cdr: ChangeDetectorRef) {}

  ngOnInit() { this.loadAll(); }

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
}
