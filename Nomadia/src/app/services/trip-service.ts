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

    
    
   
}