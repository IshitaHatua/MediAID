import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export type RefreshTopic = 'claims' | 'enrollments' | 'citizens' | 'disbursements' | 'payments' | 'schemes';

@Injectable({ providedIn: 'root' })
export class RefreshService {
  private subject = new Subject<RefreshTopic>();
  readonly events$ = this.subject.asObservable();

  notify(topic: RefreshTopic) { this.subject.next(topic); }
}
