import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { ToastrService } from 'ngx-toastr';
import { AuditManagementService, AuditService } from '../../core/services/audit.service';

@Component({
  selector: 'app-auditor-audit-logs',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatTableModule, MatFormFieldModule, MatInputModule,
    MatProgressSpinnerModule, MatTabsModule
  ],
  templateUrl: './auditor-audit-logs.component.html',
  styleUrl: './auditor-audit-logs.component.css'
})
export class AuditorAuditLogsComponent implements OnInit {

  // ── Tab 1: Business Activity Logs (from audit-management-service) ──────────
  businessLogs: any[] = [];
  allBusinessLogs: any[] = [];
  businessLoading = true;
  businessActionFilter = '';
  businessResourceFilter = '';
  businessCols = ['logId', 'userId', 'action', 'resource', 'details', 'timestamp'];

  // ── Tab 2: Identity & Access Logs (from audit-service) ────────────────────
  identityLogs: any[] = [];
  allIdentityLogs: any[] = [];
  identityLoading = true;
  identityActionFilter = '';
  identityResourceFilter = '';
  identityCols = ['auditId', 'userId', 'action', 'resource', 'details', 'timestamp'];

  accessDenied = false;

  constructor(
    private auditMgmtSvc: AuditManagementService,
    private auditSvc: AuditService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadBusinessLogs();
    this.loadIdentityLogs();
  }

  // ── Business logs ─────────────────────────────────────────────────────────

  loadBusinessLogs() {
    this.businessLoading = true;
    this.auditMgmtSvc.getLatest100Logs().subscribe({
      next: r => {
        this.allBusinessLogs = r.data ?? [];
        this.businessLogs = [...this.allBusinessLogs];
        this.businessLoading = false;
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        this.businessLoading = false;
        if (err?.status === 403 || err?.status === 401) {
          this.accessDenied = true;
        } else {
          this.toastr.error('Could not load business audit logs.');
        }
        this.cdr.markForCheck();
      }
    });
  }

  applyBusinessFilter() {
    const a = this.businessActionFilter.toLowerCase();
    const r = this.businessResourceFilter.toLowerCase();
    this.businessLogs = this.allBusinessLogs.filter(l =>
      (!a || (l.action ?? '').toLowerCase().includes(a)) &&
      (!r || (l.resource ?? '').toLowerCase().includes(r))
    );
    this.cdr.markForCheck();
  }

  clearBusinessFilter() {
    this.businessActionFilter = '';
    this.businessResourceFilter = '';
    this.businessLogs = [...this.allBusinessLogs];
    this.cdr.markForCheck();
  }

  exportBusinessCSV() {
    this.downloadCSV(this.businessLogs, 'business-audit-logs.csv');
  }

  // ── Identity logs ─────────────────────────────────────────────────────────

  loadIdentityLogs() {
    this.identityLoading = true;
    this.auditSvc.getLatest100Logs().subscribe({
      next: r => {
        this.allIdentityLogs = r.data ?? [];
        this.identityLogs = [...this.allIdentityLogs];
        this.identityLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.identityLoading = false;
        this.identityLogs = [];
        this.toastr.warning('Identity logs unavailable.');
        this.cdr.markForCheck();
      }
    });
  }

  applyIdentityFilter() {
    const a = this.identityActionFilter.toLowerCase();
    const r = this.identityResourceFilter.toLowerCase();
    this.identityLogs = this.allIdentityLogs.filter(l =>
      (!a || (l.action ?? '').toLowerCase().includes(a)) &&
      (!r || (l.resource ?? '').toLowerCase().includes(r))
    );
    this.cdr.markForCheck();
  }

  clearIdentityFilter() {
    this.identityActionFilter = '';
    this.identityResourceFilter = '';
    this.identityLogs = [...this.allIdentityLogs];
    this.cdr.markForCheck();
  }

  exportIdentityCSV() {
    this.downloadCSV(this.identityLogs, 'identity-audit-logs.csv');
  }

  // ── Shared CSV helper ─────────────────────────────────────────────────────

  private downloadCSV(data: any[], filename: string) {
    if (!data.length) return;
    const escape = (v: any) => `"${String(v ?? '').replace(/"/g, '""')}"`;
    const headers = Object.keys(data[0]).map(escape).join(',');
    const rows = data.map(row => Object.values(row).map(escape).join(','));
    const csv = [headers, ...rows].join('\r\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = filename; a.click();
    URL.revokeObjectURL(url);
  }
}
