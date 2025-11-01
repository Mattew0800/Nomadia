import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { User } from '../models/User';
import { LoginResponse } from '../models/LoginResponse';
import { RegisterResponse } from '../models/RegisterResponse';
import { putResponse } from '../models/putResponse';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  API_URL = "http://localhost:8080/nomadia";
  private tokenKey = 'token';

  users: User[];

  constructor(public http: HttpClient){
    this.users = [];
  }

  logUser(email: string, password:string){
    const body = { email, password };
    return this.http.post<LoginResponse>(`${this.API_URL}/auth/login`,body)
  }

  registerUser(name: string, email: string, password: string){
    const body = {name, email, password};
    return this.http.post<RegisterResponse>(`${this.API_URL}/auth/register`,body)
  }

  setToken(token: string) {
    localStorage.setItem(this.tokenKey, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  removeToken() {
    localStorage.removeItem(this.tokenKey);
  }










}
