import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ComplianceService } from '../../core/services/compliance.service';
import { AuthService } from '../../core/services/auth.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-compliance-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, StatusBadgeComponent],
  templateUrl: './compliance-management.component.html',
  styleUrl: './compliance-management.component.css'
})
export class ComplianceManagementComponent implements OnInit {
  all: any[] = []; violations: any[] = []; flagged: any[] = [];
  loading = true;
  evalResult: any = null;
  activeTab = 0;

  private fb = inject(FormBuilder);
  evalForm = this.fb.group({ entityId: [null, Validators.required], entityType: ['CLAIM'] });
  recordForm = this.fb.group({ entityId: [null, Validators.required], entityType: ['CLAIM'], result: ['PASS'], notes: [''] });

  constructor(private complianceSvc: ComplianceService, private authSvc: AuthService, private toastr: ToastrService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.complianceSvc.getAll().subscribe({
      next: r => { this.loading = false; this.all = r.data ?? []; this.cdr.markForCheck(); },
      error: () => { this.loading = false; this.toastr.error('Could not load compliance records.'); this.cdr.markForCheck(); }
    });
    this.complianceSvc.getViolations().subscribe({
      next: r => { this.violations = r.data ?? []; this.cdr.markForCheck(); },
      error: () => { this.violations = []; this.cdr.markForCheck(); }
    });
    this.complianceSvc.getFlagged().subscribe({
      next: r => { this.flagged = r.data ?? []; this.cdr.markForCheck(); },
      error: () => { this.flagged = []; this.cdr.markForCheck(); }
    });
  }

  evaluateFull() {
    if (this.evalForm.invalid) { this.evalForm.markAllAsTouched(); return; }
    const payload = { ...this.evalForm.value, requestedBy: Number(this.authSvc.getUserId()) };
    this.complianceSvc.evaluateFull(payload as any).subscribe({
      next: r => { this.evalResult = r.data; this.toastr.success('Evaluation complete.'); this.cdr.markForCheck(); },
      error: () => { this.toastr.error('Evaluation failed.'); this.cdr.markForCheck(); }
    });
  }

  createRecord() {
    if (this.recordForm.invalid) { this.recordForm.markAllAsTouched(); return; }
    this.complianceSvc.createRecord(this.recordForm.value as any).subscribe({
      next: r => {
        if (r.data) this.all.unshift(r.data);
        this.toastr.success('Record created.');
        this.recordForm.reset({ entityType: 'CLAIM', result: 'PASS' });
        this.cdr.markForCheck();
      },
      error: () => { this.toastr.error('Could not create record.'); this.cdr.markForCheck(); }
    });
  }
}
