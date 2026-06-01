import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AuditManagementService } from '../../core/services/audit.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-auditor-audits',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    StatusBadgeComponent
  ],
  templateUrl: './auditor-audits.component.html',
  styleUrl: './auditor-audits.component.css'
})
export class AuditorAuditsComponent implements OnInit {
  audits: any[] = [];
  filtered: any[] = [];
  loading = true;
  filterStatus = '';
  filterScope = '';

  constructor(private auditMgmtSvc: AuditManagementService, private toastr: ToastrService, private cdr: ChangeDetectorRef) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.auditMgmtSvc.getAllAudits().subscribe({
      next: r => {
        this.loading = false;
        this.audits = r.data ?? [];
        this.applyFilter();
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.toastr.error('Could not load audits.'); this.cdr.markForCheck(); }
    });
  }

  applyFilter() {
    this.filtered = this.audits.filter(a =>
      (!this.filterStatus || a.status === this.filterStatus) &&
      (!this.filterScope || a.scope === this.filterScope)
    );
  }

  clearFilters() {
    this.filterStatus = '';
    this.filterScope = '';
    this.applyFilter();
  }
}
