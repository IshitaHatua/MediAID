import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { CitizenService } from '../../core/services/citizen.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-admin-citizen-management',
  standalone: true,
  imports: [FormsModule, StatusBadgeComponent, MatDialogModule],
  templateUrl: './citizen-management.component.html',
  styleUrl: './citizen-management.component.css',
})
export class AdminCitizenManagementComponent implements OnInit {
  citizens: any[] = [];
  filteredCitizens: any[] = [];
  loading = true;
  search = '';
  selectedStatus = '';

  constructor(
    private citizenSvc: CitizenService,
    private toastr: ToastrService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.loadCitizens();
  }

  loadCitizens() {
    this.loading = true;
    this.citizenSvc.getAll().subscribe({
      next: (r) => {
        this.loading = false;
        this.citizens = r.data ?? [];
        this.applyFilter();
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.toastr.error('Could not load citizens.');
        this.cdr.markForCheck();
      },
    });
  }

  applyFilter() {
    let result = [...this.citizens];

    if (this.selectedStatus) {
      result = result.filter((c) => c.status === this.selectedStatus);
    }

    const f = this.search.trim().toLowerCase();
    if (f) {
      result = result.filter(
        (c) =>
          c.name?.toLowerCase().includes(f) ||
          String(c.citizenId).includes(f),
      );
    }

    this.filteredCitizens = result;
  }

  suspendCitizen(c: any) {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Suspend Citizen?',
        message: `Suspend "${c.name}" (ID: ${c.citizenId})? They will no longer be able to update their profile or upload documents.`,
      },
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      this.citizenSvc.suspendCitizen(c.citizenId).subscribe({
        next: (r) => {
          if (r.data) c.status = r.data.status;
          this.applyFilter();
          this.toastr.success(`Citizen "${c.name}" suspended.`);
          this.cdr.markForCheck();
        },
        error: () => {
          this.toastr.error('Could not suspend citizen.');
          this.cdr.markForCheck();
        },
      });
    });
  }
}
