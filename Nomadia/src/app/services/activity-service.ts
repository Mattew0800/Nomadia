// src/app/services/activity.service.ts
import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { Observable } from 'rxjs';
import {ActivityResponseDTO} from '../models/ActivityResponse';
import {ActivityCreateDTO} from '../models/ActivityCreate';
import {AuthService} from './auth-service';

@Injectable({ providedIn: 'root' })
export class ActivityService {

  private API_URL = 'http://localhost:8080/nomadia/activities';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private authHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    if (!token) {
      // mismo patrón que usaste en TripService
      throw new Error('No se encontró el token de autenticación. Iniciá sesión.');
    }
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  create(tripId: number, activity: ActivityCreateDTO) {
    return this.http.post<ActivityResponseDTO>(
      `${this.API_URL}/create`, { tripId, activity }, { headers: this.authHeaders() }
    );
  }

  listByTrip(tripId: number) {
    return this.http.post<ActivityResponseDTO[]>(
      `${this.API_URL}/list`, { tripId }, { headers: this.authHeaders() }
    );
  }

  // Extra por si luego lo usás
  getById(activityId: number): Observable<ActivityResponseDTO> {
    const headers = this.authHeaders();
    return this.http.post<ActivityResponseDTO>(`${this.API_URL}/get`, { activityId }, { headers });
  }
}
