import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BackendStatusService {
  private backendAvailable = new BehaviorSubject<boolean>(true);
  public backendAvailable$ = this.backendAvailable.asObservable();

  setBackendAvailable(available: boolean): void {
    this.backendAvailable.next(available);
  }

  isBackendAvailable(): boolean {
    return this.backendAvailable.value;
  }
}

