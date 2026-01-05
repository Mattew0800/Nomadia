import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Test } from '../test/test';
import { TripService } from '../../services/Trip/trip-service';
import { TripResponse } from '../../models/TripResponse';

type TripDetails = TripResponse & {
  startDate?: string;
  endDate?: string;
  description?: string;
};

@Component({
  selector: 'app-trip-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, Test],
  templateUrl: './edit-trip.html',
  styleUrls: ['./edit-trip.scss']
})
export class TripEdit implements OnInit {

  // Sólo NAME editable
  public form = new FormGroup({
    name: new FormControl<string>('', { nonNullable: true, validators: [Validators.required, Validators.minLength(3), Validators.maxLength(60)] })
  });

  submitted = false;
  msgOk?: string;
  msgError?: string;
  loading = true;

  private tripId!: string;
  trip?: TripDetails; // para mostrar campos en modo lectura

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private trips: TripService
  ) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    const stateId = navigation?.extras?.state?.['tripId'];

    this.tripId = stateId || localStorage.getItem('editTripId') || '';

    localStorage.removeItem('editTripId');

    if (!this.tripId) {
      this.msgError = 'ID de viaje inválido.';
      this.loading = false;
      this.router.navigate(['/tripList']);
      return;
    }

    this.trips.getTripById(this.tripId).subscribe({
      next: (t) => {
        this.trip = t as TripDetails;
        this.form.patchValue({ name: t.name ?? '' });
        this.loading = false;
      },
      error: () => {
        this.msgError = 'No se pudo cargar el viaje.';
        this.loading = false;
      }
    });
  }

  get f() { return this.form.controls; }

  save() {
    this.submitted = true;
    this.msgOk = undefined;
    this.msgError = undefined;

    if (this.form.invalid) {
      this.msgError = 'Revisá el nombre (mínimo 3 caracteres).';
      return;
    }

    const newName = this.form.value.name!.trim();
    if (!newName) {
      this.msgError = 'El nombre no puede estar vacío.';
      return;
    }

    // Solo actualizamos el nombre
    this.trips.updateTripName(this.tripId, newName).subscribe({
      next: () => {
        this.msgOk = 'Nombre actualizado con éxito.';
        if (this.trip) this.trip.name = newName;
      },
      error: (err) => {
        const backend = err?.error;
        this.msgError =
          err?.status === 403 ? 'No tenés permisos para editar este viaje.' :
            err?.status === 404 ? 'Viaje no encontrado.' :
              (backend?.message || 'No se pudo actualizar el nombre.');
      }
    });
  }

  cancel() { this.router.navigate(['/tripList']); }
}
