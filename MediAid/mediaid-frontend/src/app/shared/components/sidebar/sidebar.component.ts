import { Component, DoCheck } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../../core/services/auth.service';

interface NavItem { label: string; icon: string; route: string; }

const NAV_MAP: Record<string, NavItem[]> = {
  CITIZEN: [
    { label: 'Dashboard',     icon: 'dashboard',              route: '/citizen/dashboard'     },
    { label: 'My Profile',    icon: 'person',                 route: '/citizen/profile'       },
    { label: 'Enrollments',   icon: 'assignment',             route: '/citizen/enrollments'   },
    { label: 'Claims',        icon: 'receipt_long',           route: '/citizen/claims'        },
    { label: 'Disbursements', icon: 'account_balance_wallet', route: '/citizen/disbursements' },
    { label: 'Payments',      icon: 'payments',               route: '/citizen/payments'      },
  ],
  OFFICER: [
    { label: 'Dashboard',     icon: 'dashboard',              route: '/officer/dashboard'     },
    { label: 'Citizens',      icon: 'people',                 route: '/officer/citizens'      },
    { label: 'Enrollments',   icon: 'assignment',             route: '/officer/enrollments'   },
    { label: 'Claims',        icon: 'receipt_long',           route: '/officer/claims'        },
    { label: 'Disbursements', icon: 'account_balance_wallet', route: '/officer/disbursements' },
  ],
  MANAGER: [
    { label: 'Dashboard', icon: 'dashboard',   route: '/manager/dashboard' },
    { label: 'Schemes',   icon: 'local_offer', route: '/manager/schemes'   },
  ],
  ADMIN: [
    { label: 'Dashboard',       icon: 'dashboard',       route: '/admin/dashboard'   },
    { label: 'User Management', icon: 'manage_accounts', route: '/admin/users'       },
    { label: 'Audit Logs',      icon: 'manage_search',   route: '/admin/audit-logs'  },
  ],
  COMPLIANCE: [
    { label: 'Dashboard',          icon: 'dashboard',     route: '/compliance/dashboard'  },
    { label: 'Compliance Records', icon: 'verified_user', route: '/compliance/records'    },
    { label: 'Formal Audits',      icon: 'fact_check',    route: '/compliance/audits'     },
    { label: 'Audit Logs',         icon: 'manage_search', route: '/compliance/audit-logs' },
  ],
  AUDITOR: [
    { label: 'Dashboard',          icon: 'dashboard',     route: '/auditor/dashboard'  },
    { label: 'Compliance Records', icon: 'verified_user', route: '/auditor/compliance' },
    { label: 'Formal Audits',      icon: 'fact_check',    route: '/auditor/audits'     },
    { label: 'Audit Logs',         icon: 'manage_search', route: '/auditor/audit-logs' },
  ],
};

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule, MatTooltipModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements DoCheck {
  navItems: NavItem[] = [];
  private currentRole: string | null = null;

  constructor(public auth: AuthService) {
    this.refreshNav();
  }

  ngDoCheck() {
    const role = this.auth.getRole();
    if (role !== this.currentRole) this.refreshNav();
  }

  private refreshNav() {
    this.currentRole = this.auth.getRole();
    this.navItems = NAV_MAP[this.currentRole ?? ''] ?? [];
  }
}
