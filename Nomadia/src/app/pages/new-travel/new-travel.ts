import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { HttpClient } from '@angular/common/http';
import {Test} from '../test/test';
import { TripCreate } from '../../models/TripCreate';




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



  constructor(
    public authService: AuthService,
    private router: Router,
    private http: HttpClient
  ) {
   
  }

  get f() { return this.form.controls; }


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

    const formValues = this.form.getRawValue();

    const tripPayload: TripCreate = {
        name: formValues.name,
        startDate: formValues.startDate,
        endDate: formValues.endDate,
        description: formValues.description,
        type: formValues.type,

    };

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
                this.router.navigate(['tripList']);
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
