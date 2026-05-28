import { Component, ViewEncapsulation, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  encapsulation: ViewEncapsulation.None,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });
  loading = false;
  showPw = false;

  features = [
    { icon: 'shield',             text: 'Secure government-grade authentication' },
    { icon: 'receipt_long',       text: 'Real-time claims & disbursement tracking' },
    { icon: 'account_balance',    text: 'Direct bank benefit transfers' },
  ];

  constructor(private auth: AuthService, private router: Router, private toastr: ToastrService, private cdr: ChangeDetectorRef) {}

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.auth.login(this.form.value as any).subscribe({
      next: res => {
        this.loading = false;
        if (res.status === 'SUCCESS') {
          this.toastr.success('Welcome back!', 'Login Successful');
          this.router.navigate([this.auth.getDashboardRoute()]);
        } else {
          this.toastr.error(res.message || 'Login failed. Please check your credentials.');
        }
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.cdr.markForCheck(); }
    });
  }
}
