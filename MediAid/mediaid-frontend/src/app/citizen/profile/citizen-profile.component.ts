import { Component, OnInit, OnDestroy, ViewChild, ElementRef, HostListener, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpEventType } from '@angular/common/http';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { Subject, interval } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ToastrService } from 'ngx-toastr';
import { CitizenService } from '../../core/services/citizen.service';
import { AuthService } from '../../core/services/auth.service';
import { RefreshService } from '../../core/services/refresh.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

const DOC_TYPES = ['Aadhaar', 'PAN', 'Address Proof', 'Income Certificate', 'Medical Record', 'Other'];

@Component({
  selector: 'app-citizen-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatIconModule, MatTableModule, MatProgressSpinnerModule, MatDialogModule, MatDatepickerModule, StatusBadgeComponent],
  templateUrl: './citizen-profile.component.html',
  styleUrl: './citizen-profile.component.css'
})
export class CitizenProfileComponent implements OnInit, OnDestroy {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  private destroy$ = new Subject<void>();

  citizen: any = null;
  loading = true;
  saving = false;
  editing = false;
  today = new Date();

  docTypes = DOC_TYPES;
  documents: any[] = [];
  docsLoading = false;
  uploading = false;
  uploadProgress = 0;
  selectedDocType: string = DOC_TYPES[0];
  docCols = ['docType', 'fileUri', 'uploadedDate', 'verificationStatus', 'actions'];

  get hasDocument(): boolean { return this.documents.length > 0; }

  private fb = inject(FormBuilder);
  private dialog = inject(MatDialog);
  form = this.fb.group({
    name: ['', Validators.required],
    dob: ['', Validators.required],
    gender: ['', Validators.required],
    contactInfo: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    address: ['', Validators.required]
  });

  constructor(
    private citizenSvc: CitizenService,
    private auth: AuthService,
    private refresh: RefreshService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const userId = Number(this.auth.getUserId());
    this.citizenSvc.getCitizen(userId).subscribe({
      next: r => {
        this.loading = false;
        if (r.data) { this.citizen = r.data; this.loadDocs(); }
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.cdr.markForCheck(); }
    });

    // Auto-refresh document statuses every 20s so officer verifications appear without manual refresh.
    interval(20_000).pipe(takeUntil(this.destroy$)).subscribe(() => {
      if (this.citizen && !this.uploading) this.loadDocs(true);
    });

    // Same-session refresh (e.g. when this user is also acting as an officer in another tab).
    this.refresh.events$.pipe(takeUntil(this.destroy$)).subscribe(topic => {
      if (topic === 'citizens' && this.citizen) this.loadDocs(true);
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('window:focus')
  onWindowFocus() {
    if (this.citizen && !this.uploading) this.loadDocs(true);
  }

  startEdit() {
    this.editing = true;
    const patch: any = { ...this.citizen };
    // Backend uses dd-MM-yyyy strings; MatDatepicker needs a Date object.
    if (patch.dob && typeof patch.dob === 'string') {
      const parts = patch.dob.split('-');
      if (parts.length === 3) {
        const [a, b, c] = parts;
        // dd-MM-yyyy if the first part is 2 chars, otherwise yyyy-MM-dd
        const date = a.length === 2
          ? new Date(`${c}-${b}-${a}T00:00:00`)
          : new Date(`${a}-${b}-${c}T00:00:00`);
        patch.dob = isNaN(date.getTime()) ? null : date;
      }
    }
    this.form.patchValue(patch);
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const userId = Number(this.auth.getUserId());
    const raw = this.form.value as any;
    // Convert Date back to dd-MM-yyyy for the backend.
    if (raw.dob instanceof Date) {
      const d = String(raw.dob.getDate()).padStart(2, '0');
      const m = String(raw.dob.getMonth() + 1).padStart(2, '0');
      const y = raw.dob.getFullYear();
      raw.dob = `${d}-${m}-${y}`;
    } else if (raw.dob && raw.dob.includes('-') && raw.dob.indexOf('-') === 4) {
      const [y, m, d] = raw.dob.split('-');
      raw.dob = `${d}-${m}-${y}`;
    }
    const isUpdate = !!this.citizen;
    const obs = this.citizen
      ? this.citizenSvc.updateCitizen(userId, raw)
      : this.citizenSvc.createCitizen(raw);
    obs.subscribe({
      next: r => {
        this.saving = false;
        this.citizen = r.data;
        this.editing = false;
        this.toastr.success(isUpdate ? 'Profile updated!' : 'Profile created! You can now upload supporting documents below.');
        if (!isUpdate) this.loadDocs();
        this.cdr.markForCheck();
      },
      error: () => { this.saving = false; this.cdr.markForCheck(); }
    });
  }

  loadDocs(silent = false) {
    if (!this.citizen) return;
    if (!silent) this.docsLoading = true;
    this.citizenSvc.getDocuments(this.citizen.citizenId).subscribe({
      next: r => { this.docsLoading = false; this.documents = r.data ?? []; this.cdr.markForCheck(); },
      error: () => { this.docsLoading = false; this.cdr.markForCheck(); }
    });
  }

  triggerFilePicker() {
    if (this.uploading) return;
    if (this.hasDocument) {
      this.toastr.warning('Only one document is allowed. Delete the existing one to upload a new file.');
      return;
    }
    if (!this.selectedDocType) { this.toastr.error('Please choose a document type first.'); return; }
    // Reset value BEFORE opening picker so picking the same filename still fires `change`.
    if (this.fileInput?.nativeElement) {
      this.fileInput.nativeElement.value = '';
      this.fileInput.nativeElement.click();
    }
  }

  onFileSelect(event: Event) {
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
    this.uploading = true;
    this.uploadProgress = 0;
    this.citizenSvc.uploadDocumentWithProgress(this.citizen.citizenId, file, this.selectedDocType).subscribe({
      next: event => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.uploadProgress = Math.round((100 * event.loaded) / event.total);
        } else if (event.type === HttpEventType.Response) {
          this.toastr.success('Document uploaded!');
          this.loadDocs();
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.uploading = false;
        this.uploadProgress = 0;
        if (this.fileInput?.nativeElement) this.fileInput.nativeElement.value = '';
        this.cdr.markForCheck();
      },
      complete: () => {
        this.uploading = false;
        this.uploadProgress = 0;
        if (this.fileInput?.nativeElement) this.fileInput.nativeElement.value = '';
      }
    });
  }

  deleteDoc(doc: any) {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete document?',
        message: `Remove "${this.originalFileName(doc.fileUri)}" (${doc.docType})? You can upload a new one in its place.`
      }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.citizenSvc.deleteDocument(doc.documentId).subscribe({
        next: () => { this.toastr.success('Document removed.'); this.loadDocs(); this.cdr.markForCheck(); }
      });
    });
  }

  download(fileUri: string) {
    this.citizenSvc.downloadDocument(fileUri).subscribe({
      next: blob => {
        const typed = new Blob([blob], { type: this.mimeFor(fileUri) });
        const url = URL.createObjectURL(typed);
        const a = document.createElement('a');
        a.href = url; a.download = this.originalFileName(fileUri); a.click();
        URL.revokeObjectURL(url);
        this.cdr.markForCheck();
      },
      error: () => { this.toastr.error('Download failed. Please try again.'); this.cdr.markForCheck(); }
    });
  }

  originalFileName(fileUri: string): string {
    if (!fileUri) return '';
    const idx = fileUri.indexOf('_');
    return idx >= 0 ? fileUri.substring(idx + 1) : fileUri;
  }

  private mimeFor(fileUri: string): string {
    const ext = (fileUri.split('.').pop() || '').toLowerCase();
    switch (ext) {
      case 'pdf': return 'application/pdf';
      case 'png': return 'image/png';
      case 'jpg':
      case 'jpeg': return 'image/jpeg';
      case 'gif': return 'image/gif';
      default: return 'application/octet-stream';
    }
  }
}
