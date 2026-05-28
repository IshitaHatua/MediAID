import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatExpansionModule } from '@angular/material/expansion';
import { ToastrService } from 'ngx-toastr';
import { ClaimService } from '../../core/services/claim.service';
import { SchemeService } from '../../core/services/scheme.service';
import { CitizenService } from '../../core/services/citizen.service';
import { EnrollmentService } from '../../core/services/enrollment.service';
import { AuthService } from '../../core/services/auth.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-citizen-claims',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, MatCardModule, MatButtonModule, MatIconModule, MatTableModule, MatFormFieldModule, MatSelectModule, MatInputModule, MatProgressSpinnerModule, MatExpansionModule, StatusBadgeComponent],
  templateUrl: './citizen-claims.component.html',
  styleUrl: './citizen-claims.component.css'
})
export class CitizenClaimsComponent implements OnInit {
  claims: any[] = [];
  schemes: any[] = []; // Only schemes the citizen has an APPROVED, non-expired enrollment in.
  claimDocs: Record<number, any[]> = {};
  loading = true;
  profileLoading = true;
  hasProfile = false;
  citizenStatus = '';
  showDialog = false;
  pendingFile: File | null = null;

  private fb = inject(FormBuilder);
  claimForm = this.fb.group({
    schemeId: [null, Validators.required],
    claimAmount: [null, [Validators.required, Validators.min(1)]],
    description: ['']
  });

  constructor(
    private claimSvc: ClaimService,
    private schemeSvc: SchemeService,
    private citizenSvc: CitizenService,
    private enrollSvc: EnrollmentService,
    private auth: AuthService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const userId = Number(this.auth.getUserId());
    this.citizenSvc.getCitizen(userId).subscribe({
      next: r => {
        this.profileLoading = false;
        this.hasProfile = !!(r.data);
        this.citizenStatus = r.data?.status || '';
        if (this.citizenStatus === 'VERIFIED') this.load();
        this.cdr.markForCheck();
      },
      error: () => {
        this.profileLoading = false;
        this.hasProfile = false;
        this.cdr.markForCheck();
      }
    });
  }

  load() {
    this.loading = true;
    const empty = { data: [] as any[] };
    forkJoin({
      claims: this.claimSvc.getMy().pipe(catchError(() => of(empty))),
      schemes: this.schemeSvc.getAll().pipe(catchError(() => of(empty))),
      enrollments: this.enrollSvc.getMy().pipe(catchError(() => of(empty)))
    }).subscribe(({ claims, schemes, enrollments }) => {
      this.claims = claims.data ?? [];
      const allSchemes = schemes.data ?? [];
      const myEnrollments = enrollments.data ?? [];

      // Claims can only be raised against schemes the citizen has an APPROVED, non-expired enrollment in.
      // Matches the backend's validation in ClaimServiceImpl.createClaim so users don't see schemes that would fail.
      const today = new Date();
      const eligibleSchemeIds = new Set<number>(
        myEnrollments
          .filter((e: any) => e.status === 'APPROVED' && (!e.expiryDate || new Date(e.expiryDate) >= today))
          .map((e: any) => e.schemeId)
      );
      this.schemes = allSchemes.filter((s: any) => eligibleSchemeIds.has(s.schemeId));
      this.loading = false;
      this.claims.forEach(c => this.fetchDocs(c.claimId));
      this.cdr.markForCheck();
    });
  }

  schemeName(id: number) { return this.schemes.find(s => s.schemeId === id)?.name || `Scheme #${id}`; }

  originalFileName(name: string): string {
    if (!name) return '';
    const idx = name.indexOf('_');
    return idx >= 0 ? name.substring(idx + 1) : name;
  }

  openDialog() {
    this.claimForm.reset();
    this.pendingFile = null;
    this.showDialog = true;
  }

  closeDialog() {
    this.showDialog = false;
    this.pendingFile = null;
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) { this.pendingFile = null; return; }
    if (file.size > 10 * 1024 * 1024) {
      this.toastr.error('File exceeds 10MB limit.');
      input.value = '';
      this.pendingFile = null;
      return;
    }
    const allowed = ['application/pdf', 'image/jpeg', 'image/png'];
    if (file.type && !allowed.includes(file.type)) {
      this.toastr.error('Only PDF, JPG, or PNG files are allowed.');
      input.value = '';
      this.pendingFile = null;
      return;
    }
    this.pendingFile = file;
  }

  submitClaim() {
    if (this.claimForm.invalid) { this.claimForm.markAllAsTouched(); return; }
    this.claimSvc.create(this.claimForm.value as any).subscribe({
      next: r => {
        const newClaimId = r.data?.claimId;
        if (this.pendingFile && newClaimId) {
          this.claimSvc.uploadDocument(newClaimId, this.pendingFile).subscribe({
            next: () => {
              this.toastr.success('Claim submitted with document!');
              this.finishSubmit();
              this.cdr.markForCheck();
            },
            error: () => {
              this.toastr.warning('Claim submitted, but document upload failed. You can attach it later.');
              this.finishSubmit();
              this.cdr.markForCheck();
            }
          });
        } else {
          this.toastr.success('Claim submitted!');
          this.finishSubmit();
        }
      }
      // Errors on the claim create itself are surfaced by the global JWT interceptor.
    });
  }

  private finishSubmit() {
    this.closeDialog();
    this.claimForm.reset();
    this.load();
  }

  private fetchDocs(claimId: number) {
    this.claimSvc.getDocuments(claimId).subscribe({
      next: r => { this.claimDocs[claimId] = r.data ?? []; this.cdr.markForCheck(); },
      error: () => { this.claimDocs[claimId] = []; this.cdr.markForCheck(); }
    });
  }

  uploadDoc(event: Event, claimId: number) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (file.size > 10 * 1024 * 1024) {
      this.toastr.error('File exceeds 10MB limit.');
      input.value = '';
      return;
    }
    const allowed = ['application/pdf', 'image/jpeg', 'image/png'];
    if (file.type && !allowed.includes(file.type)) {
      this.toastr.error('Only PDF, JPG, or PNG files are allowed.');
      input.value = '';
      return;
    }
    this.claimSvc.uploadDocument(claimId, file).subscribe({
      next: () => {
        this.toastr.success('Document uploaded!');
        this.fetchDocs(claimId);
        this.cdr.markForCheck();
      },
      error: () => { this.toastr.error('Document upload failed.'); this.cdr.markForCheck(); },
      complete: () => { input.value = ''; }
    });
  }
}
