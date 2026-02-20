import {Component, OnInit} from '@angular/core';
import {Test} from '../test/test';
import {Router, RouterLink} from '@angular/router';
import {TripService} from '../../services/Trip/trip-service';

@Component({
  selector: 'app-trip-list',
  standalone: true,
  imports: [Test, RouterLink],
  templateUrl: './trip-list.html',
  styleUrl: './trip-list.scss',
})
export class TripList implements OnInit {

  isLoading: boolean = true;
  showingActiveTrips: boolean = true;

  // Nuevo estado para el modal de borrado
  showDeleteModal: boolean = false;
  tripToDelete: string | null = null;
  tripNameToDelete: string | null = null; // para mostrar en el modal
  isDeleting: boolean = false; // evita doble envío

  constructor(public tService: TripService, private router: Router) {

  }

  ngOnInit(): void {
    this.getTrips();
  }

  getTrips() {
    this.isLoading = true;
    return this.tService.getTrips().subscribe({
      next: (data) => {
        this.tService.trips = data;
        this.isLoading = false;
      },
      error: (e) => {
        console.log(e);
        this.isLoading = false;
      }
    })
  }

  selectTrip(tripId: string) {
    localStorage.setItem('selectedTripId', tripId.toString());

    this.router.navigate(['/mainPage']);
  }


  setFilter(isActive: boolean): void {
    this.showingActiveTrips = isActive;
  }

  get filteredViajes() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const filtered = (this.tService.trips ?? []).filter(t => {
      if (!t.endDate) return this.showingActiveTrips;

      let endDate: Date;
      if (t.endDate.includes('T')) {
        endDate = new Date(t.endDate);
      } else {
        const [year, month, day] = t.endDate.split('-').map(Number);
        endDate = new Date(year, month - 1, day);
      }
      endDate.setHours(0, 0, 0, 0);


      const isFinished = endDate < today;

      console.log(`Viaje: ${t.name}, EndDate: ${t.endDate}, EndDateParsed: ${endDate.toLocaleDateString()}, Today: ${today.toLocaleDateString()}, IsFinished: ${isFinished}`);

      return this.showingActiveTrips ? !isFinished : isFinished;
    });

    console.log(`Total viajes filtrados: ${filtered.length}`);
    return filtered;
  }



  deleteTrip(id: string, event: Event) {
    event.stopPropagation();
    this.tripToDelete = id;
    this.tripNameToDelete = this.tService.trips?.find(t => String(t.id) === String(id))?.name ?? null;
        this.showDeleteModal = true;
  }

  editTrip(id: string, event: Event) {
    event.stopPropagation();
    // Guardar el ID en localStorage y también pasarlo como state
    localStorage.setItem('editTripId', id);
    this.router.navigate(['/editTrip'], { state: { tripId: id } });
  }

  // Cancela el modal sin hacer nada
  cancelDelete() {
    // Si ya se está borrando, ignorar la acción para evitar inconsistencias
    if (this.isDeleting) return;

    this.showDeleteModal = false;
    this.tripToDelete = null;
    this.tripNameToDelete = null;
  }

  confirmDelete() {
    const id = this.tripToDelete;
    if (!id) return;

    this.isDeleting = true;

    this.tService.deleteTrip(id).subscribe({
      next: () => {
        // actualizar lista local
        this.tService.trips = this.tService.trips.filter(trip => String(trip.id) !== String(id));
        // cerrar modal y resetear estados
        this.showDeleteModal = false;
        this.tripToDelete = null;
        this.tripNameToDelete = null;
        this.isDeleting = false;
        // opcional: volver a pedir trips para garantizar consistencia
        this.getTrips();
      },
      error: (err) => {
        console.error('Error al eliminar viaje', err);
        console.log('Error status:', err?.status);
        console.log('Error error:', err?.error);
        console.log('Error error type:', typeof err?.error);
        this.isDeleting = false;

        // Priorizar el mensaje del servidor
        let msg: string;

        if (err?.error && typeof err.error === 'string') {
          msg = err.error;
        } else if (err?.error?.message) {
          msg = err.error.message;
        } else {
          switch (err?.status) {
            case 409:
              msg = 'No se puede eliminar el viaje porque tiene gastos o deudas asociadas..';
              break;
            case 403:
              msg = 'No tenés permiso para eliminar este viaje.';
              break;
            case 404:
              msg = 'El viaje ya no existe.';
              break;
            default:
              msg = 'Error al eliminar el viaje.';
          }
        }

        alert(msg);
      }
    });
  }

  get selectedTripId(): string | null {
    return localStorage.getItem('selectedTripId');
  }

  protected readonly String = String;
}
