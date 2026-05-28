import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EnrollmentService } from '../../core/services/enrollment.service';
import { SchemeService } from '../../core/services/scheme.service';
import { CitizenService } from '../../core/services/citizen.service';
import { RefreshService } from '../../core/services/refresh.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { MatDialogModule } from '@angular/material/dialog';

@Component({
  selector: 'app-enrollment-management',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatButtonModule, MatIconModule, MatTableModule, MatSelectModule, MatFormFieldModule, MatProgressSpinnerModule, MatDialogModule, StatusBadgeComponent],
  templateUrl: './enrollment-management.component.html',
  styleUrl: './enrollment-management.component.css'
})
export class EnrollmentManagementComponent implements OnInit {
  enrollments: any[] = [];
  filtered: any[] = [];
  filterStatus = '';
  loading = true;

  private citizensById: Record<number, any> = {};
  private schemesById: Record<number, any> = {};

  cols = ['citizenId', 'citizenAge', 'schemeName', 'eligibility', 'status', 'actions'];

  constructor(
    private enrollSvc: EnrollmentService,
    private schemeSvc: SchemeService,
    private citizenSvc: CitizenService,
    private refresh: RefreshService,
    private toastr: ToastrService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const empty = { data: [] as any[] };
    forkJoin({
      enrollments: this.enrollSvc.getAll().pipe(catchError(() => of(empty))),
      schemes: this.schemeSvc.getAll().pipe(catchError(() => of(empty))),
      citizens: this.citizenSvc.getAll().pipe(catchError(() => of(empty)))
    }).subscribe({
      next: ({ enrollments, schemes, citizens }) => {
        this.loading = false;
        this.enrollments = enrollments.data ?? [];
        for (const s of (schemes.data ?? [])) this.schemesById[s.schemeId] = s;
        for (const c of (citizens.data ?? [])) this.citizensById[c.citizenId] = c;
        this.applyFilter();
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.toastr.error('Could not load enrollments.'); this.cdr.markForCheck(); }
    });
  }

  applyFilter() {
    this.filtered = this.filterStatus
      ? this.enrollments.filter(e => e.status === this.filterStatus)
      : this.enrollments;
  }

  citizenAge(citizenId: number): string {
    const c = this.citizensById[citizenId];
    if (!c?.dob) return '—';
    const dob = new Date(c.dob);
    if (isNaN(dob.getTime())) return '—';
    const today = new Date();
    let age = today.getFullYear() - dob.getFullYear();
    const m = today.getMonth() - dob.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < dob.getDate())) age--;
    return age >= 0 ? String(age) : '—';
  }

  schemeName(schemeId: number): string {
    return this.schemesById[schemeId]?.name || `Scheme #${schemeId}`;
  }

  schemeEligibility(schemeId: number): string {
    return this.schemesById[schemeId]?.eligibilityCriteria || '—';
  }

  updateStatus(e: any, status: string) {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `${status === 'APPROVED' ? 'Approve' : 'Reject'} Enrollment`,
        message: `Are you sure you want to ${status.toLowerCase()} enrollment #${e.enrollmentId}?`
      }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.enrollSvc.updateStatus(e.enrollmentId, status).subscribe({
        next: r => {
          if (r.data) e.status = r.data.status;
          this.applyFilter();
          this.refresh.notify('enrollments');
          this.toastr.success(`Enrollment ${status.toLowerCase()}.`);
          this.cdr.markForCheck();
        },
        error: () => { this.toastr.error(`Could not ${status.toLowerCase()} enrollment.`); this.cdr.markForCheck(); }
      });
    });
  }
}
