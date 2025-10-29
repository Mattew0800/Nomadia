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

  users: User[];

  constructor(public http: HttpClient){
    this.users = [];
  }

  getUser(id: string){
    return this.http.get<User>(`${this.API_URL}/${id}`);
  }  

  getUsers(){
    return this.http.get<User[]>(`${this.API_URL}/user/get-all`);
  }

  logUser(email: string, password:string){
    const body = { email, password };
    return this.http.post<LoginResponse>(`${this.API_URL}/auth/login`,body)
  }

  registerUser(name: string, email: string, password: string){
    const body = {name, email, password};
    return this.http.post<RegisterResponse>(`${this.API_URL}/auth/register`,body)
  }

updateUser(data: putResponse) {  
  const token = localStorage.getItem('token');

  return this.http.put(
    `${this.API_URL}/user/me/update`,
    data,
    {
      headers: {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` })
      }
    }
  );
}


  
}
