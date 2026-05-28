import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule, RouterModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  users: any[] = []; totalUsers = 0; citizens = 0; officers = 0; managers = 0;
  summaryCards: any[] = [];
  loading = true;

  constructor(private userSvc: UserService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.refreshCards();
    this.userSvc.getAll().subscribe({
      next: r => {
        const data = r.data ?? [];
        this.users = data;
        this.totalUsers = data.length;
        this.citizens = data.filter((u: any) => u.role === 'CITIZEN').length;
        this.officers = data.filter((u: any) => u.role === 'OFFICER').length;
        this.managers = data.filter((u: any) => u.role === 'MANAGER').length;
        this.loading = false;
        this.refreshCards();
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.cdr.markForCheck(); }
    });
  }

  refreshCards() {
    this.summaryCards = [
      { label: 'Total Users', value: this.totalUsers, icon: 'people', grad: 'linear-gradient(135deg,#6366f1,#4f46e5)', glow: '#6366f1' },
      { label: 'Citizens', value: this.citizens, icon: 'person', grad: 'linear-gradient(135deg,#059669,#10b981)', glow: '#10b981' },
      { label: 'Officers', value: this.officers, icon: 'badge', grad: 'linear-gradient(135deg,#f59e0b,#d97706)', glow: '#f59e0b' },
      { label: 'Managers', value: this.managers, icon: 'supervisor_account', grad: 'linear-gradient(135deg,#8b5cf6,#7c3aed)', glow: '#8b5cf6' },
    ];
  }

  roleClass(role: string): string {
    const map: Record<string, string> = { CITIZEN: 'role-citizen', OFFICER: 'role-officer', MANAGER: 'role-manager', ADMIN: 'role-admin' };
    return map[role] || 'role-citizen';
  }
}
