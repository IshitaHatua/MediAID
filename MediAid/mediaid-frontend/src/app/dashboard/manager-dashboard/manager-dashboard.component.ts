import { Component, OnInit, ChangeDetectorRef } from '@angular/core';

import { RouterModule } from '@angular/router';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SchemeService } from '../../core/services/scheme.service';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './manager-dashboard.component.html',
  styleUrl: './manager-dashboard.component.css',
})
export class ManagerDashboardComponent implements OnInit {
  totalSchemes = 0;
  activeSchemes = 0;
  summaryCards: any[] = [];
  loading = true;

  constructor(
    private schemeSvc: SchemeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.refreshCards();
    this.schemeSvc
      .getAll()
      .pipe(catchError(() => of({ data: [] as any[] })))
      .subscribe((schemes) => {
        const sData = schemes.data ?? [];
        this.totalSchemes = sData.length;
        this.activeSchemes = sData.filter((s: any) => s.status === 'ACTIVE').length;
        this.loading = false;
        this.refreshCards();
        this.cdr.markForCheck();
      });
  }

  refreshCards() {
    this.summaryCards = [
      { label: 'Total Schemes', value: this.totalSchemes, icon: '⚑' },
      { label: 'Active Schemes', value: this.activeSchemes, icon: '✓' },
    ];
  }
}
