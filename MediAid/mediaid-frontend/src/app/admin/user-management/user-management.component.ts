import { Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../../core/services/user.service';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, MatCardModule, MatButtonModule, MatIconModule, MatTableModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatPaginatorModule, MatSortModule, MatProgressSpinnerModule, MatDialogModule],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css'
})
export class UserManagementComponent implements OnInit {
  dataSource = new MatTableDataSource<any>([]);
  loading = true;
  search = '';
  editingUser: any = null;
  cols = ['userId', 'name', 'email', 'role', 'actions'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(private userSvc: UserService, private toastr: ToastrService, private dialog: MatDialog, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.userSvc.getAll().subscribe({
      next: r => {
        this.loading = false;
        if (r.data) {
          this.dataSource.data = r.data;
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
        }
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.cdr.markForCheck(); }
    });
  }

  applyFilter() {
    this.dataSource.filter = this.search.trim().toLowerCase();
    this.dataSource.filterPredicate = (u, filter) =>
      u.name?.toLowerCase().includes(filter) || u.email?.toLowerCase().includes(filter);
  }

  startEdit(u: any) { this.editingUser = { ...u }; }

  saveEdit(original: any) {
    const roleChanged = this.editingUser.role !== original.role;
    // Always include role in the update payload to satisfy backend validation
    this.userSvc.update(this.editingUser.userId, {
      name: this.editingUser.name,
      email: this.editingUser.email,
      role: this.editingUser.role
    }).subscribe({
      next: r => {
        Object.assign(original, r.data ?? this.editingUser);
        if (roleChanged) {
          this.userSvc.updateRole(original.userId, this.editingUser.role).subscribe({
            next: r2 => { original.role = r2.data?.role ?? this.editingUser.role; this.cdr.markForCheck(); }
          });
        }
        this.editingUser = null;
        this.toastr.success('User updated.');
        this.cdr.markForCheck();
      },
      error: err => {
        this.toastr.error(err?.error?.message || 'Update failed.');
        this.cdr.markForCheck();
      }
    });
  }

  deleteUser(u: any) {
    const ref = this.dialog.open(ConfirmDialogComponent, { data: { title: 'Delete User', message: `Delete user "${u.name}"? This cannot be undone.` } });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.userSvc.delete(u.userId).subscribe({
        next: () => {
          this.dataSource.data = this.dataSource.data.filter((x: any) => x.userId !== u.userId);
          this.toastr.success('User deleted.');
          this.cdr.markForCheck();
        },
        error: () => { this.toastr.error('Could not delete user.'); this.cdr.markForCheck(); }
      });
    });
  }
}
