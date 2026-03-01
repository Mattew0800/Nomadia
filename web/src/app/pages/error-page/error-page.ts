import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BackendStatusService } from '../../services/Backend-Status/backend-status.service';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-error-page',
  imports: [CommonModule],
  templateUrl: './error-page.html',
  styleUrl: './error-page.scss',
  standalone: true
})
export class ErrorPage implements OnInit, OnDestroy {
  errorType: string = '404';
  errorTitle: string = '404';
  errorMessage: string = 'Esta no es la página que estás buscando...';
  showRetryButton: boolean = false;
  showLoader: boolean = false;

  private backendStatusSubscription?: Subscription;
  private previousRoute: string = '/login';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private backendStatus: BackendStatusService
  ) {}

  ngOnInit(): void {
    // Obtener el tipo de error de los query params
    this.route.queryParams.subscribe(params => {
      const type = params['type'];

      if (type === 'backend-unavailable') {
        this.errorType = 'backend';
        this.errorTitle = 'Conexión perdida';
        this.errorMessage = 'Intentando restablecer la conexión con el servidor...';
        this.showRetryButton = false;
        this.showLoader = true;

        // Determinar a dónde volver: si viene de una API URL, extraer la ruta de Angular correspondiente
        const from = params['from'] ?? '';
        if (from.includes('/nomadia/auth/register')) {
          this.previousRoute = '/register';
        } else if (from.includes('/nomadia/auth/login')) {
          this.previousRoute = '/login';
        } else if (from.includes('/nomadia/')) {
          this.previousRoute = '/mainPage';
        }

        this.subscribeToBackendStatus();
      } else {
        this.errorType = '404';
        this.errorTitle = '404';
        this.errorMessage = 'Esta no es la página que estás buscando...';
        this.showRetryButton = false;
        this.showLoader = false;
      }
    });
  }

  private subscribeToBackendStatus(): void {
    this.backendStatusSubscription = this.backendStatus.backendAvailable$.subscribe(available => {
      if (available && this.errorType === 'backend') {
        console.log('✅ Backend disponible nuevamente, redirigiendo...');
        this.errorTitle = '¡Conexión restablecida!';
        this.errorMessage = 'Redirigiendo...';

        // Esperar un momento antes de redirigir para que el usuario vea el mensaje
        setTimeout(() => {
          this.router.navigate([this.previousRoute]);
        }, 1000);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.backendStatusSubscription) {
      this.backendStatusSubscription.unsubscribe();
    }
  }

  retry(): void {
    // Método legacy por si acaso se usa en otros contextos
    this.backendStatus.forceHealthCheck();
  }

  goHome(): void {
    this.router.navigate(['/landing']);
  }
}
