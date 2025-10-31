import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { User } from '../../models/User';
import { UserService } from '../../services/user-service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-user-profile',
  imports: [RouterLink, DatePipe],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.css',
})
export class UserProfile implements OnInit{

  user?: User

  constructor(public authService: AuthService, private router: Router, public userService: UserService) { 

  }

  ngOnInit() {
    this.userService.getCurrentUser().subscribe({
      next: (userData: User) => {
        this.user = userData;
      },
      error: (err) => {
        console.error('Error al obtener el usuario', err);
      }
    });
  }

  logout() {
    localStorage.removeItem('token');
    this.authService.users = [];
    this.router.navigate(['/login']);
  }


}
