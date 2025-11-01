// src/app/services/trip.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AuthService } from './auth-service'; 
import { TripCreateDTO, TripResponseDTO } from '../models/trip.model';


@Injectable({
  providedIn: 'root'
})
export class TripService {
    
    
    private apiUrl = 'http://localhost:8080/nomadia/trip'; 

    constructor(
        private http: HttpClient,
        private authService: AuthService
    ) { }

    /**
     * Llama al endpoint POST /trip/create para registrar un nuevo viaje.
     * @param tripData Los datos del viaje a crear (TripCreateDTO).
     * @returns Un Observable con la respuesta del viaje creado.
     */
    createTrip(tripData: TripCreateDTO): Observable<TripResponseDTO> {
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

        return this.http.post<TripResponseDTO>(
            `${this.apiUrl}/create`,
            tripData,
            { headers: headers }
        );
    }
    
   
}