import { Component, OnInit } from '@angular/core';
import { Test } from '../test/test';
import { RouterLink } from '@angular/router';
import { TripService } from '../../services/trip-service';

@Component({
  selector: 'app-trip-list',
  imports: [Test, RouterLink],
  templateUrl: './trip-list.html',
  styleUrl: './trip-list.css',
})
export class TripList implements OnInit{

  
  showingActiveTrips: boolean = true; 

  constructor(public tService: TripService){

  }

  ngOnInit(): void {
    this.getTrips();
    console.log(this.tService.trips);
  }

  getTrips(){
    return this.tService.getTrips().subscribe({
      next: (data) => {
        this.tService.trips = data;        
      },
      error: (e) => {console.log(e);}
    })
  }

    
    setFilter(isActive: boolean): void {
        this.showingActiveTrips = isActive;
    }
    
    get filteredViajes() {
        const targetState = this.showingActiveTrips ? 'CONFIRMADO' : 'FINALIZADO';
        return this.tService.trips.filter(viaje => viaje.state === targetState);
    }


}