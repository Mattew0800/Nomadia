// src/app/services/trip.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AuthService } from './auth-service';
import { TripResponse } from '../models/TripResponse';
import { TripCreate } from '../models/TripCreate';


@Injectable({
  providedIn: 'root'
})
export class TripService {


    private API_URL = 'http://localhost:8080/nomadia/trip';
    trips : TripResponse[];

    constructor(
        private http: HttpClient,
        private authService: AuthService
    ) {
        this.trips = [];
     }

    /**
     * Llama al endpoint POST /trip/create para registrar un nuevo viaje.
     * @param tripData Los datos del viaje a crear (TripCreateDTO).
     * @returns Un Observable con la respuesta del viaje creado.
     */
    createTrip(tripData: TripCreate): Observable<TripResponse> {
        const token = this.authService.getToken();

        if (!token) {
             return new Observable(observer => {
                observer.error(new Error('No se encontró el token de autenticación. Por favor, inicie sesión.'));
             });
        }

        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        });

        return this.http.post<TripResponse>(
            `${this.API_URL}/create`,
            tripData,
            { headers: headers }
        );
    }

    getTrips() {
        const token = localStorage.getItem('token'); // o donde lo guardes
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.get<TripResponse[]>(`${this.API_URL}/my-trips`, { headers });
    }

  deleteTrip(id: number) {
    const token = localStorage.getItem('token'); // o donde lo guardes

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.delete(`${this.API_URL}/delete`, { body: { tripId: id }, headers: headers });

  }


  getTripById(id: string): Observable<TripResponse> {
    const token = this.authService.getToken();
    if (!token) {
      return new Observable(observer => {
        observer.error(new Error('No se encontró el token de autenticación. Iniciá sesión.'));
      });
    }
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
    // Usando tu endpoint POST /view-trip
    return this.http.post<TripResponse>(`${this.API_URL}/view-trip`, { tripId: id }, { headers });
  }

  updateTripName(tripId: string, name: string) {
    const token = this.authService.getToken();
    if (!token) {
      return new Observable(observer => {
        observer.error(new Error('No se encontró el token de autenticación. Iniciá sesión.'));
      });
    }
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    // Enviamos el resto de campos igual que estaban (si no querés tocarlos en back)
    const dto = { name, tripId }; // si tu TripUpdateDTO exige más, completalos en el componente con los valores originales

    return this.http.put(`${this.API_URL}/update`, dto, { headers });
  }





}
