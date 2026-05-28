import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
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
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-formal-audit-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, MatButtonModule, MatIconModule, MatTableModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatTabsModule, MatExpansionModule, MatProgressSpinnerModule, StatusBadgeComponent],
  templateUrl: './formal-audit-management.component.html',
  styleUrl: './formal-audit-management.component.css'
})
export class FormalAuditManagementComponent implements OnInit {
  audits: any[] = [];
  filtered: any[] = [];
  logs: any[] = [];
  loading = true;
  logsLoading = true;
  showCreateForm = false;
  filterStatus = '';
  filterScope = '';
  logCols = ['logId', 'userId', 'action', 'resource', 'timestamp'];

  private fb = inject(FormBuilder);
  auditForm = this.fb.group({
    officerId: [null, Validators.required],
    scope: ['CLAIM', Validators.required],
    scopeEntityId: [null, Validators.required],
    findings: ['']
  });

  constructor(private auditMgmtSvc: AuditManagementService, private toastr: ToastrService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.auditMgmtSvc.getAllAudits().subscribe({
      next: r => {
        this.loading = false;
        this.audits = (r.data ?? []).map((a: any) => ({ ...a, _newStatus: a.status }));
        this.applyFilter();
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.toastr.error('Could not load audits.'); this.cdr.markForCheck(); }
    });
    this.auditMgmtSvc.getLogs().subscribe({
      next: r => { this.logsLoading = false; this.logs = r.data ?? []; this.cdr.markForCheck(); },
      error: () => { this.logsLoading = false; this.logs = []; this.cdr.markForCheck(); }
    });
  }

  applyFilter() {
    this.filtered = this.audits.filter(a =>
      (!this.filterStatus || a.status === this.filterStatus) &&
      (!this.filterScope || a.scope === this.filterScope)
    );
  }

  createAudit() {
    if (this.auditForm.invalid) { this.auditForm.markAllAsTouched(); return; }
    this.auditMgmtSvc.createAudit(this.auditForm.value as any).subscribe({
      next: r => {
        if (r.data) this.audits.unshift({ ...r.data, _newStatus: r.data.status });
        this.applyFilter();
        this.showCreateForm = false;
        this.auditForm.reset({ scope: 'CLAIM' });
        this.toastr.success('Audit created!');
        this.cdr.markForCheck();
      },
      error: () => { this.toastr.error('Could not create audit.'); this.cdr.markForCheck(); }
    });
  }

  updateAudit(a: any) {
    this.auditMgmtSvc.updateAudit(a.auditId, { status: a._newStatus }).subscribe({
      next: r => { if (r.data) a.status = r.data.status; this.applyFilter(); this.toastr.success('Audit updated.'); this.cdr.markForCheck(); },
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
