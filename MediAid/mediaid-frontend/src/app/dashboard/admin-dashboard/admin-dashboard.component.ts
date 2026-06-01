import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
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
      { label: 'Total Users', value: this.totalUsers, icon: '\u{1F465}' },
      { label: 'Citizens', value: this.citizens, icon: '\u{1F464}' },
      { label: 'Officers', value: this.officers, icon: '★' },
      { label: 'Managers', value: this.managers, icon: '⚑' },
    ];
  }

  roleClass(role: string): string {
    const map: Record<string, string> = { CITIZEN: 'citizen', OFFICER: 'officer', MANAGER: 'manager', ADMIN: 'admin' };
    return map[role] || 'citizen';
  }
}
