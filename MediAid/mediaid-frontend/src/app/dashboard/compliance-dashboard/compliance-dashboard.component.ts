import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ComplianceService } from '../../core/services/compliance.service';

@Component({
  selector: 'app-compliance-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule, RouterModule],
  templateUrl: './compliance-dashboard.component.html',
  styleUrl: './compliance-dashboard.component.css'
})
export class ComplianceDashboardComponent implements OnInit {
  total = 0; passed = 0; violations = 0; flagged = 0;
  summaryCards: any[] = [];
  loading = true;

  constructor(private complianceSvc: ComplianceService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.refreshCards();
    const empty = { data: [] as any[] };
    forkJoin({
      all: this.complianceSvc.getAll().pipe(catchError(() => of(empty))),
      violations: this.complianceSvc.getViolations().pipe(catchError(() => of(empty))),
      flagged: this.complianceSvc.getFlagged().pipe(catchError(() => of(empty)))
    }).subscribe(({ all, violations, flagged }) => {
      const allData = all.data ?? [];
      this.total = allData.length;
      this.violations = (violations.data ?? []).length;
      this.flagged = (flagged.data ?? []).length;
      this.passed = allData.filter((r: any) => (r.result || '').toUpperCase() === 'PASS').length;
      this.loading = false;
      this.refreshCards();
      this.cdr.markForCheck();
    });
  }

  refreshCards() {
    this.summaryCards = [
      { label: 'Total Records', value: this.total,      icon: 'rule',           grad: 'linear-gradient(135deg,#6366f1,#4f46e5)', glow: '#6366f1' },
      { label: 'Passed',        value: this.passed,     icon: 'check_circle',   grad: 'linear-gradient(135deg,#10b981,#059669)', glow: '#10b981' },
      { label: 'Violations',    value: this.violations, icon: 'warning',        grad: 'linear-gradient(135deg,#ef4444,#dc2626)', glow: '#ef4444' },
      { label: 'Flagged',       value: this.flagged,    icon: 'flag',           grad: 'linear-gradient(135deg,#f59e0b,#d97706)', glow: '#f59e0b' },
    ];
  }
}
