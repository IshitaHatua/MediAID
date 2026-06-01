import { Component, OnInit, ChangeDetectorRef } from '@angular/core';

import { RouterModule } from '@angular/router';
import { AuditManagementService } from '../../core/services/audit.service';

@Component({
  selector: 'app-auditor-dashboard',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './auditor-dashboard.component.html',
  styleUrl: './auditor-dashboard.component.css',
})
export class AuditorDashboardComponent implements OnInit {
  mgmtLogCount = 0;
  uniqueActions = 0;
  uniqueResources = 0;
  summaryCards: any[] = [];
  loading = true;

  constructor(
    private auditMgmtSvc: AuditManagementService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.refreshCards();
    this.auditMgmtSvc.getLogs().subscribe({
      next: (r) => {
        const mgmtLogs = r.data ?? [];
        this.mgmtLogCount = mgmtLogs.length;
        this.uniqueActions = new Set(mgmtLogs.map((l: any) => l.action).filter(Boolean)).size;
        this.uniqueResources = new Set(mgmtLogs.map((l: any) => l.resource).filter(Boolean)).size;
        this.loading = false;
        this.refreshCards();
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  refreshCards() {
    this.summaryCards = [
      { label: 'Audit Management Logs', value: this.mgmtLogCount, icon: '⚲' },
      { label: 'Unique Actions', value: this.uniqueActions, icon: '✎' },
      { label: 'Unique Resources', value: this.uniqueResources, icon: '☰' },
    ];
  }
}
