import {Component, OnInit} from '@angular/core';
import {Test} from '../test/test';
import {Router, RouterLink} from '@angular/router';
import {TripService} from '../../services/trip-service';

@Component({
  selector: 'app-trip-list',
  standalone: true,
  imports: [Test, RouterLink],
  templateUrl: './trip-list.html',
  styleUrl: './trip-list.css',
})
export class TripList implements OnInit {


  showingActiveTrips: boolean = true;

  constructor(public tService: TripService, private router: Router) {

  }

  ngOnInit(): void {
    this.getTrips();
    console.log(this.tService.trips);
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
    // se guarda el id en local storage para mantenerlo oculto
    localStorage.setItem('selectedTripId', tripId.toString());

    this.router.navigate(['/mainPage']);
  }


  setFilter(isActive: boolean): void {
    this.showingActiveTrips = isActive;
  }

  get filteredViajes() {
    const targetState = this.showingActiveTrips ? 'CONFIRMADO' : 'FINALIZADO';
    return this.tService.trips.filter(t => t.state === targetState);
  }

  deleteTrip(id: string, event: Event) {
    event.stopPropagation(); // evita que haga clic sobre la card
    if (confirm('¿Seguro que querés eliminar este viaje?')) {
      this.tService.deleteTrip(id).subscribe({
        next: () => {
          this.tService.trips = this.tService.trips.filter(trip => trip.id !== id);
          this.getTrips()
        },
        error: (err) => {
          console.error('Error al eliminar viaje', err);
        }
      });
    }
  }

  protected readonly String = String;
}
