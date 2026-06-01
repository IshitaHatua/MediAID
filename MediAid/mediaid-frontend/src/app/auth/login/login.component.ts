import { Component, ViewEncapsulation, inject, ChangeDetectorRef } from '@angular/core';

import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  encapsulation: ViewEncapsulation.None,
  imports: [ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });
  loading = false;
  showPw = false;

  constructor(
    private auth: AuthService,
    private router: Router,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef,
  ) {}

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.auth.login(this.form.value as any).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.status === 'SUCCESS') {
          if (this.auth.markUserSeen()) {
            this.toastr.success('Welcome back!', 'Login Successful');
          } else {
            this.toastr.success('Welcome to MediAID!', 'Login Successful');
          }
          this.router.navigate([this.auth.getDashboardRoute()]);
        } else {
          this.toastr.error(res.message || 'Login failed. Please check your credentials.');
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }
}
