import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, interval, Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class BackendStatusService {
  private http = inject(HttpClient);

  private backendAvailable = new BehaviorSubject<boolean>(true);
  public backendAvailable$ = this.backendAvailable.asObservable();

  private retrySubscription?: Subscription;
  private retryInterval = 3000;

  setBackendAvailable(available: boolean): void {
    this.backendAvailable.next(available);

    if (!available) {
      this.startAutoRetry();
    } else {
      this.stopAutoRetry();
    }
  }

  isBackendAvailable(): boolean {
    return this.backendAvailable.value;
  }

  private startAutoRetry(): void {
    if (this.retrySubscription && !this.retrySubscription.closed) {
      return;
    }

    console.log('🔄 Iniciando reintento automático cada', this.retryInterval / 1000, 'segundos...');

    this.retrySubscription = interval(this.retryInterval).subscribe(() => {
      this.checkBackendHealth();
    });
  }

  private stopAutoRetry(): void {
    if (this.retrySubscription) {
      this.retrySubscription.unsubscribe();
      console.log('✅ Reintento automático detenido - Backend disponible');
    }
  }

  private checkBackendHealth(): void {
    const healthUrl = 'http://localhost:8080/nomadia/health';

    this.http.get(healthUrl, {
      observe: 'response',
    }).subscribe({
      next: () => {
        console.log('✅ Backend respondió correctamente');
        this.setBackendAvailable(true);
      },
      error: (err) => {
        // Si es un 404, significa que el backend responde pero no tiene ese endpoint
        // En ese caso, consideramos que el backend está disponible
        if (err.status === 404 || err.status === 401 || err.status === 403) {
          console.log('✅ Backend respondió (estado:', err.status, ')');
          this.setBackendAvailable(true);
        } else {
          console.log('⏳ Backend aún no disponible, reintentando...');
        }
      }
    });
  }

  // Método público para forzar un chequeo manual
  public forceHealthCheck(): void {
    this.checkBackendHealth();
  }

  ngOnDestroy(): void {
    this.stopAutoRetry();
  }
}

