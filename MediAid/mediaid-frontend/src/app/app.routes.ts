import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./home/home.component').then(m => m.HomeComponent) },
  {
    path: 'auth',
    children: [
      { path: 'login', loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent) },
      { path: 'register', loadComponent: () => import('./auth/register/register.component').then(m => m.RegisterComponent) },
      { path: 'forgot-password', loadComponent: () => import('./auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
      { path: 'reset-password', loadComponent: () => import('./auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent) },
    ]
  },
  {
    path: 'citizen',
    canActivate: [authGuard, roleGuard(['CITIZEN'])],
    children: [
      { path: 'dashboard', loadComponent: () => import('./dashboard/citizen-dashboard/citizen-dashboard.component').then(m => m.CitizenDashboardComponent) },
      { path: 'profile', loadComponent: () => import('./citizen/profile/citizen-profile.component').then(m => m.CitizenProfileComponent) },
      { path: 'enrollments', loadComponent: () => import('./citizen/enrollments/citizen-enrollments.component').then(m => m.CitizenEnrollmentsComponent) },
      { path: 'claims', loadComponent: () => import('./citizen/claims/citizen-claims.component').then(m => m.CitizenClaimsComponent) },
      { path: 'disbursements', loadComponent: () => import('./citizen/disbursements/citizen-disbursements.component').then(m => m.CitizenDisbursementsComponent) },
      { path: 'payments', loadComponent: () => import('./citizen/payments/citizen-payments.component').then(m => m.CitizenPaymentsComponent) },
    ]
  },
  {
    path: 'officer',
    canActivate: [authGuard, roleGuard(['OFFICER'])],
    children: [
      { path: 'dashboard', loadComponent: () => import('./dashboard/officer-dashboard/officer-dashboard.component').then(m => m.OfficerDashboardComponent) },
      { path: 'citizens', loadComponent: () => import('./officer/citizen-management/citizen-management.component').then(m => m.CitizenManagementComponent) },
      { path: 'enrollments', loadComponent: () => import('./officer/enrollment-management/enrollment-management.component').then(m => m.EnrollmentManagementComponent) },
      { path: 'claims', loadComponent: () => import('./officer/claim-management/claim-management.component').then(m => m.ClaimManagementComponent) },
      { path: 'disbursements', loadComponent: () => import('./officer/disbursement-management/disbursement-management.component').then(m => m.DisbursementManagementComponent) },
    ]
  },
  {
    path: 'manager',
    canActivate: [authGuard, roleGuard(['MANAGER'])],
    children: [
      { path: 'dashboard', loadComponent: () => import('./dashboard/manager-dashboard/manager-dashboard.component').then(m => m.ManagerDashboardComponent) },
      { path: 'schemes', loadComponent: () => import('./manager/scheme-management/scheme-management.component').then(m => m.SchemeManagementComponent) },
    ]
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard(['ADMIN'])],
    children: [
      { path: 'dashboard', loadComponent: () => import('./dashboard/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
      { path: 'users', loadComponent: () => import('./admin/user-management/user-management.component').then(m => m.UserManagementComponent) },
      { path: 'audit-logs', loadComponent: () => import('./admin/audit-logs/audit-logs.component').then(m => m.AuditLogsComponent) },
    ]
  },
  {
    path: 'auditor',
    canActivate: [authGuard, roleGuard(['AUDITOR'])],
    children: [
      { path: 'dashboard',   loadComponent: () => import('./dashboard/auditor-dashboard/auditor-dashboard.component').then(m => m.AuditorDashboardComponent) },
      { path: 'audit-logs',  loadComponent: () => import('./auditor/audit-logs/auditor-audit-logs.component').then(m => m.AuditorAuditLogsComponent) },
      { path: 'audits',      loadComponent: () => import('./auditor/audits/auditor-audits.component').then(m => m.AuditorAuditsComponent) },
      { path: 'compliance',  loadComponent: () => import('./auditor/compliance/auditor-compliance.component').then(m => m.AuditorComplianceComponent) },
    ]
  },
  {
    path: 'compliance',
    canActivate: [authGuard, roleGuard(['COMPLIANCE'])],
    children: [
      { path: 'dashboard',   loadComponent: () => import('./dashboard/compliance-dashboard/compliance-dashboard.component').then(m => m.ComplianceDashboardComponent) },
      { path: 'records',     loadComponent: () => import('./compliance/records/compliance-records.component').then(m => m.ComplianceRecordsComponent) },
      { path: 'audits',      loadComponent: () => import('./compliance/audits/compliance-audits.component').then(m => m.ComplianceAuditsComponent) },
      { path: 'audit-logs',  loadComponent: () => import('./compliance/audit-logs/compliance-audit-logs.component').then(m => m.ComplianceAuditLogsComponent) },
    ]
  },
  { path: 'unauthorized', loadComponent: () => import('./shared/components/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent) },
  { path: '**', redirectTo: '' }
];
