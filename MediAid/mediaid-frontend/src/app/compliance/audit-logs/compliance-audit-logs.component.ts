import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AuditManagementService, AuditService } from '../../core/services/audit.service';

@Component({
  selector: 'app-compliance-audit-logs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compliance-audit-logs.component.html',
  styleUrl: './compliance-audit-logs.component.css'
})
export class ComplianceAuditLogsComponent implements OnInit {

  activeTab: 'business' | 'identity' = 'business';

  businessLogs: any[] = [];
  allBusinessLogs: any[] = [];
  businessLoading = true;
  businessActionFilter = '';
  businessResourceFilter = '';

  identityLogs: any[] = [];
  allIdentityLogs: any[] = [];
  identityLoading = true;
  identityActionFilter = '';
  identityResourceFilter = '';

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
