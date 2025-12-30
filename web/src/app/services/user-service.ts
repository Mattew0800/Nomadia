import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {User} from '../models/User';
import {LoginResponse} from '../models/LoginResponse';
import {RegisterResponse} from '../models/RegisterResponse';
import {putResponse} from '../models/putResponse';
import {UpdateUserResponse} from '../models/UpdateUserResponse';
import {AuthService} from './auth-service';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  API_URL = "http://localhost:8080/nomadia";

  users: User[];

  constructor(public http: HttpClient, private authService: AuthService) {
    this.users = [];
  }

  getUser(id: string) {
    return this.http.get<User>(`${this.API_URL}/${id}`);
  }

  getUsers() {
    return this.http.get<User[]>(`${this.API_URL}/user/get-all`);
  }

  getCurrentUser() {
    return this.http.get<User>(`${this.API_URL}/user/me`, {
      headers: this.authService.authHeaders(),
    });
  }

  updateUser(data: putResponse) {
    return this.http.put<UpdateUserResponse>(
      `${this.API_URL}/user/me/update`,
      data,
      {headers: this.authService.authHeaders()}
    );
  }


}
