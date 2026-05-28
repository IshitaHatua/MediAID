import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './status-badge.component.html',
  styleUrl: './status-badge.component.css'
})
export class StatusBadgeComponent {
  @Input() status: string = '';

  get colorClass(): string {
    const s = (this.status || '').toUpperCase();
    if (['VERIFIED', 'APPROVED', 'ACTIVE', 'COMPLETED', 'PASS', 'DISBURSED'].includes(s)) return 'green';
    if (['REJECTED', 'FAILED', 'FAIL'].includes(s)) return 'red';
    if (['SUSPENDED'].includes(s)) return 'purple';
    if (['PENDING'].includes(s)) return 'amber';
    if (['FLAGGED', 'ESCALATED', 'IN_PROGRESS'].includes(s)) return 'yellow';
    return 'blue';
  }
}
