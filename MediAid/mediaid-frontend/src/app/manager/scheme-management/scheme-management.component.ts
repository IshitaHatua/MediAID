import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { SchemeService } from '../../core/services/scheme.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-scheme-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, MatCardModule, MatButtonModule, MatIconModule, MatTableModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatProgressSpinnerModule, MatDialogModule, StatusBadgeComponent],
  templateUrl: './scheme-management.component.html',
  styleUrl: './scheme-management.component.css'
})
export class SchemeManagementComponent implements OnInit {
  schemes: any[] = [];
  filtered: any[] = [];
  loading = true;
  showForm = false;
  editMode = false;
  search = '';
  cols = ['name', 'maxCoverageAmount', 'validityYears', 'status', 'actions'];

  private fb = inject(FormBuilder);
  schemeForm = this.fb.group({
    name: ['', Validators.required],
    description: ['', Validators.required],
    eligibilityCriteria: ['', Validators.required],
    benefits: ['', Validators.required],
    maxCoverageAmount: [null, [Validators.required, Validators.min(1)]],
    validityYears: [1, [Validators.required, Validators.min(1)]],
    status: ['ACTIVE', Validators.required]
  });

  constructor(private schemeSvc: SchemeService, private toastr: ToastrService, private dialog: MatDialog, private cdr: ChangeDetectorRef) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.schemeSvc.getAll().subscribe({
      next: r => { this.loading = false; this.schemes = r.data ?? []; this.applyFilter(); this.cdr.markForCheck(); },
      error: () => { this.loading = false; this.toastr.error('Could not load schemes.'); this.cdr.markForCheck(); }
    });
  }

  applyFilter() {
    this.filtered = this.search
      ? this.schemes.filter(s => s.name.toLowerCase().includes(this.search.toLowerCase()))
      : this.schemes;
  }

  resetForm() { this.editMode = false; this.schemeForm.reset({ validityYears: 1, status: 'ACTIVE' }); }

  submit() {
    if (this.schemeForm.invalid) { this.schemeForm.markAllAsTouched(); return; }
    this.schemeSvc.create(this.schemeForm.value as any).subscribe({
      next: () => {
        this.toastr.success('Scheme created!');
        this.showForm = false;
        this.schemeForm.reset({ validityYears: 1, status: 'ACTIVE' });
        this.load();
        this.cdr.markForCheck();
      },
      error: () => { this.toastr.error('Could not create scheme.'); this.cdr.markForCheck(); }
    });
  }

  toggleStatus(s: any) {
    const newStatus = s.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.schemeSvc.updateStatus(s.schemeId, { status: newStatus }).subscribe({
      next: r => {
        if (r.data) s.status = r.data.status;
        this.toastr.success(`Scheme ${newStatus.toLowerCase()}.`);
        this.cdr.markForCheck();
      },
      error: () => { this.toastr.error('Could not update scheme status.'); this.cdr.markForCheck(); }
    });
  }

  deleteScheme(s: any) {
    const ref = this.dialog.open(ConfirmDialogComponent, { data: { title: 'Delete Scheme', message: `Delete scheme "${s.name}"? This cannot be undone.` } });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.schemeSvc.delete(s.schemeId).subscribe({
        next: () => { this.toastr.success('Scheme deleted.'); this.load(); this.cdr.markForCheck(); },
        error: () => { this.toastr.error('Could not delete scheme.'); this.cdr.markForCheck(); }
      });
    });
  }
}
