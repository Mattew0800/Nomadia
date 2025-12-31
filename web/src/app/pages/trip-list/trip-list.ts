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


  showingActiveTrips: boolean = true;
  msgError?: string;

  constructor(public tService: TripService, private router: Router) {

  }

  ngOnInit(): void {
    this.getTrips();
  }

  getTrips() {
    return this.tService.getTrips().subscribe({
      next: (data) => {
        this.tService.trips = data;
      },
      error: (e) => {
        console.log(e);
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
      if (!t.endDate) return this.showingActiveTrips; // Si no tiene fecha, mostrar en activos

      // Parsear la fecha de fin como fecha local (sin problemas de zona horaria)
      // Si viene como 'YYYY-MM-DD', parsearlo correctamente
      let endDate: Date;
      if (t.endDate.includes('T')) {
        // Tiene timestamp, usar constructor normal
        endDate = new Date(t.endDate);
      } else {
        // Es solo fecha 'YYYY-MM-DD', parsear como fecha local
        const [year, month, day] = t.endDate.split('-').map(Number);
        endDate = new Date(year, month - 1, day);
      }
      endDate.setHours(0, 0, 0, 0);

      // Un viaje está finalizado si su fecha de fin ya pasó (es decir, hoy es DESPUÉS de la fecha de fin)
      // Si endDate es 30/12, el viaje está activo el 30/12 y finalizado a partir del 31/12
      const isFinished = endDate < today;

      console.log(`Viaje: ${t.name}, EndDate: ${t.endDate}, EndDateParsed: ${endDate.toLocaleDateString()}, Today: ${today.toLocaleDateString()}, IsFinished: ${isFinished}`);

      // Si showingActiveTrips es true, mostrar viajes activos (no finalizados)
      // Si showingActiveTrips es false, mostrar viajes finalizados
      return this.showingActiveTrips ? !isFinished : isFinished;
    });

    console.log(`Total viajes filtrados: ${filtered.length}`);
    return filtered;
  }

  deleteTrip(id: string, event: Event) {
    event.stopPropagation();
    if (confirm('¿Seguro que querés eliminar este viaje?')) {
      this.tService.deleteTrip(id).subscribe({
        next: () => {
          this.tService.trips = this.tService.trips.filter(trip => trip.id !== id);
          this.getTrips()
        },
        error: (err) => {
          console.error('Error al eliminar viaje', err);
          alert(err.error);
        }
      });
    }
  }

  protected readonly String = String;
}
