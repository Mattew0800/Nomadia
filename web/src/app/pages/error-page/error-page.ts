import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BackendStatusService } from '../../services/Backend-Status/backend-status.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-error-page',
  imports: [CommonModule],
  templateUrl: './error-page.html',
  styleUrl: './error-page.scss',
  standalone: true
})
export class ErrorPage implements OnInit {
  errorType: string = '404';
  errorTitle: string = '404';
  errorMessage: string = 'Esta no es la página que estás buscando...';
  showRetryButton: boolean = false;

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
        this.errorTitle = 'Servidor no disponible';
        this.errorMessage = 'No se puede conectar con el servidor. Por favor, verifica que el backend esté iniciado e intenta nuevamente.';
        this.showRetryButton = true;
      } else {
        this.errorType = '404';
        this.errorTitle = '404';
        this.errorMessage = 'Esta no es la página que estás buscando...';
        this.showRetryButton = false;
      }
    });
  }

  retry(): void {
    // Marcar backend como disponible nuevamente
    this.backendStatus.setBackendAvailable(true);

    // Intentar volver a la página principal
    this.router.navigate(['/mainPage']);
  }

  goHome(): void {
    this.router.navigate(['/landing']);
  }
}
