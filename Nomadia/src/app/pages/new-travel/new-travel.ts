import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { HttpClient } from '@angular/common/http';
import {Test} from '../test/test';

// --- DTO TEMPORAL (Coincide con nomadia.DTO.Trip.TripCreateDTO) ---
interface TempTripCreateDTO {
    name: string;
    startDate: string;
    endDate: string;
    description: string;
    type: string;
    budget?: number; // Sigue en el DTO, pero se enviará como undefined/null desde aquí
    // Nota: El campo 'destino' de tu formulario NO se mapea al DTO de backend
}
// -------------------------------------------------------------------

type AgendaItem = { time: string; label: string; desc: string; color: 'yellow'|'purple'|'blue' };

function dateRangeValidator(group: AbstractControl): ValidationErrors | null {
  const desde = group.get('startDate')?.value as string | null; // Corregido el nombre del control
  const hasta = group.get('endDate')?.value as string | null;   // Corregido el nombre del control
  if (!desde || !hasta) return null;
  const d = new Date(desde);
  const h = new Date(hasta);
  return h >= d ? null : { dateRange: true };
}

@Component({
  selector: 'app-new-travel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, Test],
  templateUrl: './new-travel.html',
  styleUrls: ['./new-travel.css'],
})
export class NewTravel {

  // --- FORMULARIO CORREGIDO SIN 'budget' ---
  form = new FormGroup({
    name: new FormControl<string>('', { nonNullable: true, validators: [Validators.required, Validators.minLength(3)] }),
    type: new FormControl<string>('CLASICO', { nonNullable: true, validators: [Validators.required] }),
    startDate: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    endDate: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    description: new FormControl<string>('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(100)] }), // Description ahora es required
  }, { validators: dateRangeValidator });
  // -------------------------------------------

  submitted = false;
  msgOk?: string;
  msgError?: string;
  loading = false;

  tripTypes: string[] = ['CLASICO', 'AVENTURA', 'RELAX', 'TRABAJO'];

  // Se ha quitado todo el código de calendario/agenda para centrarse en la funcionalidad del viaje
  // ... si necesitas el código de agenda, insértalo aquí.

  constructor(
    public authService: AuthService,
    private router: Router,
    private http: HttpClient
  ) {
    // Si tenías lógica de calendario aquí, recupérala si es necesaria
  }

  // Getter para acceder a los controles del formulario fácilmente en el HTML
  get f() { return this.form.controls; }


  // --- FUNCIÓN SAVE AJUSTADA SIN 'budget' ---
  save() {
    this.submitted = true;
    this.msgError = undefined;
    this.msgOk = undefined;
    this.loading = true;

    if (this.form.invalid) {
      this.loading = false;
      if (this.form.hasError('dateRange')) {
        this.msgError = 'La fecha "Hasta" debe ser posterior a "Desde".';
      } else {
        this.msgError = 'Revisá los campos obligatorios.';
      }
      return;
    }

    // 1. Obtener valores del formulario
    const formValues = this.form.getRawValue();

    // 2. Mapear y preparar el payload para el backend
    // Excluimos 'destino' y creamos el payload con los nombres de campos del DTO de Java
    const tripPayload: TempTripCreateDTO = {
        name: formValues.name,
        startDate: formValues.startDate,
        endDate: formValues.endDate,
        description: formValues.description,
        type: formValues.type,
        // budget se omite aquí, por lo que será 'undefined' en el objeto,
        // y Spring Boot lo interpretará como null o usará el valor por defecto
        // (que se ignora si no se envía).
    };

    // 3. Simular la llamada HTTP
    const apiUrl = 'http://localhost:8080/nomadia/trip/create';
    const token = this.authService.getToken();

    if (!token) {
        this.loading = false;
        this.msgError = 'Error de autenticación. No se encontró el token.';
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };

    this.http.post<any>(apiUrl, tripPayload, { headers })
        .subscribe({
            next: (response) => {
                this.loading = false;
                console.log('Viaje creado:', response);
                this.msgOk = 'Viaje creado con éxito.';
                this.form.reset({ type: 'CLASICO' });
                this.submitted = false;
            },
            error: (err) => {
                this.loading = false;
                console.error('Error del backend:', err);
                this.msgError = err.error?.message || 'Error al conectar con el servidor.';
            }
        });
  }
  // -----------------------------------------------------------------

  logout() {
    localStorage.removeItem('token');
    this.authService.users = [];
    this.router.navigate(['/login']);
  }
}
