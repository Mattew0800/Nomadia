import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
  ValidatorFn
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/Auth/auth-service';
import { HttpClient } from '@angular/common/http';
import {Test} from '../test/test';
import { TripService } from '../../services/Trip/trip-service';
import { TripCreate } from '../../models/TripCreate';


function dateRangeValidator(group: AbstractControl): ValidationErrors | null {
  const desde = group.get('startDate')?.value as string | null; // Corregido el nombre del control
  const hasta = group.get('endDate')?.value as string | null;   // Corregido el nombre del control
  if (!desde || !hasta) return null;
  const d = new Date(desde);
  const h = new Date(hasta);
  return h > d ? null : { dateRange: true };
}

@Component({
  selector: 'app-new-travel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, Test],
  templateUrl: './new-travel.html',
  styleUrls: ['./new-travel.scss'],
})
export class NewTravel {

  currentStep: number=0;

  // Tipos de viaje con iconos y nombres mejorados
  tripTypesWithIcons = [
    { value: 'turismo', label: 'Turismo', icon: '🗺️' },
    { value: 'aventura', label: 'Aventura', icon: '🏔️' },
    { value: 'gastronomico', label: 'Gastronómico', icon: '🍽️' },
    { value: 'educativo', label: 'Educativo', icon: '📚' },
    { value: 'familiar', label: 'Familiar', icon: '👨‍👩‍👧‍👦' },
    { value: 'relax', label: 'Relax', icon: '🧘' },
    { value: 'romantico', label: 'Romántico', icon: '💑' },
    { value: 'cultural', label: 'Cultural', icon: '🏛️' },
    { value: 'playa', label: 'Playa', icon: '🏖️' },
    { value: 'deportivo', label: 'Deportivo', icon: '⚽' },
    { value: 'voluntariado', label: 'Voluntariado', icon: '🤝' },
    { value: 'fiesta', label: 'Fiesta', icon: '🎉' },
    { value: 'profesional', label: 'Profesional', icon: '💼' }
  ];

  // --- FORMULARIO CORREGIDO SIN 'budget' ---
  form = new FormGroup({
    name: new FormControl<string>('', { nonNullable: true, validators: [Validators.required, Validators.minLength(3)] }),
    type: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    startDate: new FormControl<string>('', { nonNullable: true, validators: [Validators.required, this.pastDateValidator()]}),
    endDate: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    description: new FormControl<string>('', { validators: [Validators.maxLength(100)] }),
  }, { validators: dateRangeValidator });
  // -------------------------------------------

  submitted = false;
  msgOk?: string;
  msgError?: string;
  loading = false;

 get f() { return this.form.controls; }

  constructor(
    public authService: AuthService,
    private router: Router,
    private http: HttpClient,
    private tripService: TripService
  ) {
  }

  get tripTypeClass(): string {
    const typeControl = this.form.get('type');

    if (!typeControl || !typeControl.value) {
      return '';
    }

    return `trip-type-${typeControl.value.toUpperCase()}`;
  }

  get tripTypeLabel(): string {
    const typeValue = this.form.get('type')?.value;
    if (!typeValue) return '';

    const tripType = this.tripTypesWithIcons.find(t => t.value.toUpperCase() === typeValue);
    return tripType ? tripType.label : typeValue;
  }

  public get nameControl() {
    return this.form.get('name')!;
  }

  public get typeControl() {
    return this.form.get('type')!;
  }



 selectTripType(type: string): void {
    this.form.get('type')?.setValue(type.toUpperCase());
    this.currentStep = 1;
  }


  /**
   * Permite volver al paso de selección de tipo (Paso 0).
   */
  goToStep(step: number): void {
    this.currentStep = step;
    if (step === 0) {
        this.form.get('type')?.setValue(''); // Limpiar el tipo
        this.form.markAsPristine();
        this.submitted = false;
        this.msgError = undefined;
    }
  }


  public pastDateValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const dateValue = control.value;

      if (!dateValue) {
        return null;
      }

      const inputDate = new Date(dateValue);
      const today = new Date();

      today.setHours(0, 0, 0, 0);
      inputDate.setHours(0, 0, 0, 0);

      // Para viajes, la fecha NO debe ser anterior a hoy
      if (inputDate < today) {
        return { pastDate: true };
      }

      return null;
    };
  }


  // --- FUNCIÓN SAVE AJUSTADA SIN 'budget' ---
  save() {
    this.submitted = true;
    this.msgError = undefined;
    this.msgOk = undefined;
    this.loading = true;

    if (this.form.invalid) {
      this.loading = false;
      this.form.markAllAsTouched();
      return;
    }

    // 1. Obtener valores del formulario
    const formValues = this.form.getRawValue();

    // 2. Mapear y preparar el payload para el backend
    // Excluimos 'destino' y creamos el payload con los nombres de campos del DTO de Java
    const tripPayload: TripCreate = {
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

    this.tripService.createTrip(tripPayload)
        .subscribe({
            next: (response) => {
                this.loading = false;
                console.log('Viaje creado:', response);
                this.msgOk = 'Viaje creado con éxito.';

                // Resetea el formulario y vuelve al Paso 0
                this.form.reset({ type: '' });
                this.goToStep(0);
                this.submitted = false;
                this.router.navigate(['tripList'])
            },
            error: (err) => {
                this.loading = false;
                console.error('Error del backend:', err);
                // Usamos el mensaje de error del backend
                this.msgError = err.error || 'Error al conectar con el servidor. Verifica tu token.';
                this.submitted = false;
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
