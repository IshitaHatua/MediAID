import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../../core/services/user.service';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, MatDialogModule],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css'
})
export class UserManagementComponent implements OnInit {
  users: any[] = [];
  filteredUsers: any[] = [];
  loading = true;
  search = '';
  editingUser: any = null;

  constructor(private userSvc: UserService, private toastr: ToastrService, private dialog: MatDialog, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.userSvc.getAll().subscribe({
      next: r => {
        this.loading = false;
        if (r.data) {
          this.users = r.data;
          this.filteredUsers = [...this.users];
        }
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.cdr.markForCheck(); }
    });
  }

  applyFilter() {
    const f = this.search.trim().toLowerCase();
    this.filteredUsers = !f
      ? [...this.users]
      : this.users.filter(u =>
          u.name?.toLowerCase().includes(f) || u.email?.toLowerCase().includes(f));
  }

  startEdit(u: any) { this.editingUser = { ...u }; }

  saveEdit(original: any) {
    const roleChanged = this.editingUser.role !== original.role;
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
          this.users = this.users.filter((x: any) => x.userId !== u.userId);
          this.applyFilter();
          this.toastr.success('User deleted.');
          this.cdr.markForCheck();
        },
        error: () => { this.toastr.error('Could not delete user.'); this.cdr.markForCheck(); }
      });
    });
  }
}
