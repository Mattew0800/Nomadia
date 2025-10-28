import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-user-profile',
  imports: [],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.css',
})
export class UserProfile {

    constructor(public authService: AuthService,private router: Router){}


    logout() {
      localStorage.removeItem('token');
      this.authService.users = [];
      this.router.navigate(['/login']);
    }


}
