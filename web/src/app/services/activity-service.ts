// src/app/services/activity.service.ts
import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { Observable } from 'rxjs';
import {ActivityResponseDTO} from '../models/ActivityResponse';
import {ActivityCreateDTO} from '../models/ActivityCreate';
import {ActivityUpdateDTO} from '../models/ActivityUpdateDTO';
import {AuthService} from './auth-service';

@Injectable({ providedIn: 'root' })
export class ActivityService {

  private API_URL = 'http://localhost:8080/nomadia/activities';

  activities : ActivityCreateDTO[];

  constructor(private http: HttpClient, private authService: AuthService) {
    this.activities = [];
  }


  create(activity: ActivityCreateDTO) {

    return this.http.post(
      `${this.API_URL}/create`,
      activity,
      { headers: this.authService.authHeaders() }
    );
  }

  listByTrip(tripId: number) {
    return this.http.post<ActivityResponseDTO[]>(
      `${this.API_URL}/list`,
      { tripId },
      { headers: this.authService.authHeaders() }
    );
  }


  listMine(): Observable<ActivityResponseDTO[]> {
    return this.http.post<ActivityResponseDTO[]>(
      `http://localhost:8080/nomadia/activities/list`,
      {},
      { headers:  this.authService.authHeaders() }
    );
  }

  listMineFiltered(body: {
    fromDate?: string;
    toDate?: string;
    fromTime?: string;
    toTime?: string;
  }): Observable<ActivityResponseDTO[]> {
    return this.http.post<ActivityResponseDTO[]>(
      `http://localhost:8080/nomadia/activities/list`,
      body,
      { headers:  this.authService.authHeaders() }
    );
  }

  deleteActivity(tripId: string, activityId: string) {

    const headers = {headers:  this.authService.authHeaders()};
    const body = {tripId, activityId};

    return this.http.post(
      `${this.API_URL}/delete`,
      body,
      headers);
  }

  updateActivity(tripId: string, activityId: string, dto: ActivityUpdateDTO): Observable<ActivityResponseDTO> {
    const payload: ActivityUpdateDTO = {
      ...dto,
      activityId: activityId,
      tripId: tripId
    };

    return this.http.put<ActivityResponseDTO>(
      `${this.API_URL}/update`,
      payload,
      { headers: this.authService.authHeaders() }
    );
  }
}
