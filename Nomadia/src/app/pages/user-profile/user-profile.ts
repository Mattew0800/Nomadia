import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-user-profile',
  imports: [RouterLink],
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
