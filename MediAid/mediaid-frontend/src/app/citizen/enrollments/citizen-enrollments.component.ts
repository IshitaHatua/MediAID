import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastrService } from 'ngx-toastr';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EnrollmentService } from '../../core/services/enrollment.service';
import { SchemeService } from '../../core/services/scheme.service';
import { CitizenService } from '../../core/services/citizen.service';
import { AuthService } from '../../core/services/auth.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-citizen-enrollments',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, MatCardModule, MatButtonModule, MatIconModule, MatTableModule, MatDialogModule, MatFormFieldModule, MatSelectModule, MatProgressSpinnerModule, StatusBadgeComponent],
  templateUrl: './citizen-enrollments.component.html',
  styleUrl: './citizen-enrollments.component.css'
})
export class CitizenEnrollmentsComponent implements OnInit {
  enrollments: any[] = [];
  schemes: any[] = [];
  loading = true;
  schemesLoading = true;
  profileLoading = true;
  hasProfile = false;
  citizenStatus = '';
  showDialog = false;
  cols = ['enrollmentId', 'schemeId', 'enrollmentDate', 'expiryDate', 'status'];

  private fb = inject(FormBuilder);
  enrollForm = this.fb.group({ schemeId: [null, Validators.required] });

  constructor(
    private enrollSvc: EnrollmentService,
    private schemeSvc: SchemeService,
    private citizenSvc: CitizenService,
    private auth: AuthService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const userId = Number(this.auth.getUserId());
    this.citizenSvc.getCitizen(userId).subscribe({
      next: r => {
        this.profileLoading = false;
        this.hasProfile = !!(r.data);
        this.citizenStatus = r.data?.status || '';
        if (this.citizenStatus === 'VERIFIED') this.load();
        this.cdr.markForCheck();
      },
      error: () => {
        this.profileLoading = false;
        this.hasProfile = false;
        this.cdr.markForCheck();
      }
    });
  }

  load() {
    this.loading = true;
    this.schemesLoading = true;
    const empty = { data: [] as any[] };
    forkJoin({
      enrollments: this.enrollSvc.getMy().pipe(catchError(() => of(empty))),
      schemes: this.schemeSvc.getAll().pipe(catchError(() => of(empty)))
    }).subscribe(({ enrollments, schemes }) => {
      this.enrollments = enrollments.data ?? [];
      const allSchemes = schemes.data ?? [];
      this.schemes = allSchemes.filter((s: any) => s.status?.toUpperCase() === 'ACTIVE');
      if (this.schemes.length === 0) this.schemes = allSchemes;
      this.loading = false;
      this.schemesLoading = false;
      this.cdr.markForCheck();
    });
  }

  schemeName(id: number) { return this.schemes.find(s => s.schemeId === id)?.name || `Scheme #${id}`; }

  openEnrollDialog() { this.enrollForm.reset(); this.showDialog = true; }

  submitEnroll() {
    if (this.enrollForm.invalid) { this.enrollForm.markAllAsTouched(); return; }
    this.enrollSvc.enroll(this.enrollForm.value as any).subscribe({
      next: () => {
        this.toastr.success('Enrollment submitted!');
        this.showDialog = false;
        this.enrollForm.reset();
        this.load();
        this.cdr.markForCheck();
      },
      error: () => { this.toastr.error('Could not submit enrollment.'); this.cdr.markForCheck(); }
    });
  }
}
