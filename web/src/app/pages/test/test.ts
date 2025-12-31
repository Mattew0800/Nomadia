import { Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/Auth/auth-service';
import { User } from '../../models/User';
import { UserService } from '../../services/User/user-service';

type AgendaItem = { time: string; label: string; desc: string; color: 'yellow'|'purple'|'blue' };


@Component({
  selector: 'app-test',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './test.html',
  styleUrls: ['./test.scss'],
})
export class Test implements OnInit{

  constructor(private router: Router, public userService: UserService, public authService: AuthService){}

    // --- DROPDOWN PERFIL ---
  showMenu = false;
  user?: User

  @ViewChild('userMenu', { static: false }) menuRef!: ElementRef<HTMLElement>;
  @ViewChild('userMenuBtn', { static: false }) btnRef!: ElementRef<HTMLElement>;

  toggleMenu(event: MouseEvent) {
    event.stopPropagation();
    this.showMenu = !this.showMenu;
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

  onSelect(action: string) {
    this.showMenu = false;
    switch (action) {
    case 'profile':
      this.router.navigate(['/profile']);
      break;
    case 'test':
      this.router.navigate(['/test']); // si tenés esta ruta
      break;
    case 'cerrar':
      this.router.navigate(['/login']);
      break;
    default:
      console.warn('Acción desconocida:', action);
  }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as Node;
    const clickedInsideMenu = this.menuRef?.nativeElement.contains(target);
    const clickedButton = this.btnRef?.nativeElement.contains(target);
    if (!clickedInsideMenu && !clickedButton) {
      this.showMenu = false;
    }
  }

  @HostListener('document:keydown.escape')
  onEsc() {
    this.showMenu = false;
  }

  isRouteActive(routePath: string): boolean {
    return this.router.url.includes(routePath);
  }



   logout() {
    localStorage.removeItem('token');
    this.authService.users = [];
    this.router.navigate(['/login']);
  }
}
