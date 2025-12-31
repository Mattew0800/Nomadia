import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/Auth/auth-service';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-login-page',
  imports: [RouterLink, ReactiveFormsModule, NgClass],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPage{

    logForm: FormGroup;
    loginError: string | null = null;
    invalidCredentials = false;

    constructor(private authService: AuthService, private router: Router){
      this.logForm = new FormGroup({
        email: new FormControl(),
        password: new FormControl()
      })
    }


  login() {

    const { email, password } = this.logForm.value;

    this.authService.logUser(email, password).subscribe({
      next: (res) => {
        console.log("LOGUEADO JOYA");
        localStorage.setItem('token', res.token);
        this.authService.users = [{
          name: res.name,
          email: res.email,
          password: ''
        }];
        this.invalidCredentials = false;
        this.loginError = null;
        this.router.navigate(['/profile']);
      },
      error: (e) => {
        console.log(e);
        this.invalidCredentials = true;
        this.loginError = 'Email o contraseña incorrectos';
      }
    });
  }


}
