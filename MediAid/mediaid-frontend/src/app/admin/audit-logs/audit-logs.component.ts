import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditService } from '../../core/services/audit.service';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './audit-logs.component.html',
  styleUrl: './audit-logs.component.css'
})
export class AuditLogsComponent implements OnInit {
  basicLogs: any[] = [];
  allBasicLogs: any[] = [];
  basicLoading = true;
  userIdFilter = '';
  actionFilter = '';

  constructor(private auditSvc: AuditService) {}

  ngOnInit() { this.load(); }

  load() {
    this.basicLoading = true;
    this.auditSvc.getLatest100Logs().subscribe({
      next: r => {
        this.allBasicLogs = r.data ?? [];
        this.basicLogs = [...this.allBasicLogs];
        this.basicLoading = false;
      },
      error: () => {
        this.basicLoading = false;
        this.basicLogs = [];
      }
    });
  }

  applyFilter() {
    const a = this.actionFilter.toLowerCase();
    const u = this.userIdFilter.toLowerCase();
    this.basicLogs = this.allBasicLogs.filter(l =>
      (!a || (l.action ?? '').toLowerCase().includes(a)) &&
      (!u || (l.resource ?? '').toLowerCase().includes(u)
           || (l.userId ?? '').toString().includes(u))
    );
  }

  clear() {
    this.userIdFilter = '';
    this.actionFilter = '';
    this.basicLogs = [...this.allBasicLogs];
  }

  exportCSV(data: any[], filename: string) {
    if (!data.length) return;
    const escape = (v: any) => {
      const s = v === null || v === undefined ? '' : String(v);
      return `"${s.replace(/"/g, '""')}"`;
    };
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
