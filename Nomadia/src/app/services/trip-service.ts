import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AuthService} from './auth-service';
import {TripResponse} from '../models/TripResponse';
import {TripCreate} from '../models/TripCreate';
import {User} from '../models/User';
import {TravelerResponse} from '../models/TravelerResponse';


@Injectable({
  providedIn: 'root'
})
export class TripService {


  private API_URL = 'http://localhost:8080/nomadia/trip';
  trips: TripResponse[];
  users: TravelerResponse[];

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {
    this.trips = [];
    this.users = [];
  }


  createTrip(tripData: TripCreate): Observable<TripResponse> {
    return this.http.post<TripResponse>(
      `${this.API_URL}/create`,
      tripData,
      {headers: this.authService.authHeaders()}
    );
  }

  getTrips() {
    return this.http.get<TripResponse[]>(`${this.API_URL}/my-trips`, {headers: this.authService.authHeaders()});
  }

  deleteTrip(id: string) {
    return this.http.delete(`${this.API_URL}/delete`, {body: {tripId: id}, headers: this.authService.authHeaders()});
  }

  getTripById(id: string): Observable<TripResponse> {

    return this.http.post<TripResponse>(
      `${this.API_URL}/view-trip`,
      {tripId: id},
      {headers: this.authService.authHeaders()});
  }

  updateTripName(tripId: string, name: string) {
    const dto = {name, tripId};

    return this.http.put(
      `${this.API_URL}/update`,
      dto,
      {headers: this.authService.authHeaders()});
  }

  addUser(tripId: string, email: string) {
    const body = {tripId: tripId, email: email};

    return this.http.post<TravelerResponse>(
      `${this.API_URL}/add-user`,
      body,
      {headers: this.authService.authHeaders()});
  }

  getUsers(tripId: string){
    return this.http.post<TravelerResponse[]>(
      `${this.API_URL}/get-travelers`,
      {tripId: tripId},
      {headers: this.authService.authHeaders()});
  }






}
