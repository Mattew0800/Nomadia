import { Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/Auth/auth-service';
import { User } from '../../models/User';
import { UserService } from '../../services/User/user-service';
import { FormsModule } from '@angular/forms';
import { ActivityService } from '../../services/Activity/activity-service';
import { ActivityResponseDTO } from '../../models/ActivityResponse';

type AgendaItem = { time: string; label: string; desc: string; color: 'yellow'|'purple'|'blue' };


@Component({
  selector: 'app-test',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './test.html',
  styleUrls: ['./test.scss'],
})
export class Test implements OnInit{

  searchQuery: string = ''; // Texto del buscador
  allActivities: ActivityResponseDTO[] = []; // Lista completa para comparar
  filteredSuggestions: ActivityResponseDTO[] = [];
  showSuggestions: boolean = false;

  constructor(private router: Router, public userService: UserService, public authService: AuthService, private activityService: ActivityService ){}

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

    this.loadActivities();
  }

  loadActivities() {
    this.activityService.listMine().subscribe({
      next: (list) => {
        this.allActivities = list ?? [];
      },
      error: (e) => console.error('Error cargando actividades para el buscador', e)
    });
  }

  onInputChange() {
    const term = this.searchQuery.trim().toLowerCase();

    if (term.length >= 1) {
      this.filteredSuggestions = this.allActivities
        .filter(a => a.name.toLowerCase().includes(term))
        .slice(0, 10); // Mostramos solo las primeras 5 sugerencias
      this.showSuggestions = true;
    } else {
      this.showSuggestions = false;
    }
  }

  selectSuggestion(activity: ActivityResponseDTO) {
    this.searchQuery = activity.name;
    this.showSuggestions = false;
    this.router.navigate(['/activities'], { queryParams: { search: activity.name } });
  }

  onSearch() {
    this.showSuggestions = false;
    const term = this.searchQuery.trim().toLowerCase();

    if (!term) return; // Si no hay texto, no hace nada

    // Buscamos si hay alguna actividad que coincida con el nombre
    this.router.navigate(['/activities'], { queryParams: { search: term } });

    this.searchQuery = '';
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
    this.showSuggestions = false;
  }

  @HostListener('document:keydown.escape')
  onEsc() {
    this.showMenu = false;
    this.showSuggestions = false;
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
