import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SchemeService } from '../../core/services/scheme.service';
import { ComplianceService } from '../../core/services/compliance.service';
import { AuditManagementService } from '../../core/services/audit.service';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule, RouterModule],
  templateUrl: './manager-dashboard.component.html',
  styleUrl: './manager-dashboard.component.css'
})
export class ManagerDashboardComponent implements OnInit {
  totalSchemes = 0; activeSchemes = 0; violations = 0; totalAudits = 0;
  summaryCards: any[] = [];
  loading = true;

  constructor(
    private schemeSvc: SchemeService,
    private complianceSvc: ComplianceService,
    private auditMgmtSvc: AuditManagementService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.refreshCards();
    const empty = { data: [] as any[] };
    forkJoin({
      schemes: this.schemeSvc.getAll().pipe(catchError(() => of(empty))),
      violations: this.complianceSvc.getViolations().pipe(catchError(() => of(empty))),
      audits: this.auditMgmtSvc.getAllAudits().pipe(catchError(() => of(empty)))
    }).subscribe(({ schemes, violations, audits }) => {
      const sData = schemes.data ?? [];
      this.totalSchemes = sData.length;
      this.activeSchemes = sData.filter((s: any) => s.status === 'ACTIVE').length;
      this.violations = (violations.data ?? []).length;
      this.totalAudits = (audits.data ?? []).length;
      this.loading = false;
      this.refreshCards();
      this.cdr.markForCheck();
    });
  }

  refreshCards() {
    this.summaryCards = [
      { label: 'Total Schemes', value: this.totalSchemes, icon: 'local_offer', grad: 'linear-gradient(135deg,#6366f1,#4f46e5)', glow: '#6366f1' },
      { label: 'Active Schemes', value: this.activeSchemes, icon: 'check_circle', grad: 'linear-gradient(135deg,#059669,#10b981)', glow: '#10b981' },
      { label: 'Compliance Violations', value: this.violations, icon: 'warning', grad: 'linear-gradient(135deg,#ef4444,#dc2626)', glow: '#ef4444' },
      { label: 'Formal Audits', value: this.totalAudits, icon: 'fact_check', grad: 'linear-gradient(135deg,#f59e0b,#d97706)', glow: '#f59e0b' },
    ];
  }
}
