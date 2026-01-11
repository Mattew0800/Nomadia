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
  msgError?: string;

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

  editTrip(id: string, event: Event) {
    event.stopPropagation();
    // Guardar el ID en localStorage y también pasarlo como state
    localStorage.setItem('editTripId', id);
    this.router.navigate(['/editTrip'], { state: { tripId: id } });
  }

  protected readonly String = String;
}
