import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuditManagementService } from '../../core/services/audit.service';

@Component({
  selector: 'app-auditor-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule, RouterModule],
  templateUrl: './auditor-dashboard.component.html',
  styleUrl: './auditor-dashboard.component.css'
})
export class AuditorDashboardComponent implements OnInit {
  mgmtLogCount = 0;
  uniqueActions = 0;
  uniqueResources = 0;
  summaryCards: any[] = [];
  loading = true;

  constructor(private auditMgmtSvc: AuditManagementService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.refreshCards();
    this.auditMgmtSvc.getLogs().subscribe({
      next: r => {
        const mgmtLogs = r.data ?? [];
        this.mgmtLogCount = mgmtLogs.length;
        this.uniqueActions = new Set(mgmtLogs.map((l: any) => l.action).filter(Boolean)).size;
        this.uniqueResources = new Set(mgmtLogs.map((l: any) => l.resource).filter(Boolean)).size;
        this.loading = false;
        this.refreshCards();
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.cdr.markForCheck(); }
    });
  }

  refreshCards() {
    this.summaryCards = [
      { label: 'Audit Management Logs', value: this.mgmtLogCount,   icon: 'manage_search', grad: 'linear-gradient(135deg,#0ea5e9,#0284c7)', glow: '#38bdf8' },
      { label: 'Unique Actions',        value: this.uniqueActions,  icon: 'gesture',       grad: 'linear-gradient(135deg,#f59e0b,#d97706)', glow: '#f59e0b' },
      { label: 'Unique Resources',      value: this.uniqueResources, icon: 'folder',        grad: 'linear-gradient(135deg,#10b981,#059669)', glow: '#10b981' },
    ];
  }
}
