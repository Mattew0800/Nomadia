import {Component, HostListener, ElementRef, ViewChild, HostBinding, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterLink} from '@angular/router';
import {Test} from '../test/test';
import {TripService} from '../../services/trip-service';
import {TripResponse} from '../../models/TripResponse';

type AgendaItem = { time: string; label: string; desc: string; color: 'yellow' | 'purple' | 'blue' };


@Component({
  selector: 'app-main-page',
  standalone: true,
  imports: [CommonModule, Test],
  templateUrl: './main-page.html',
  styleUrls: ['./main-page.css'],
})
export class MainPage implements OnInit {

  activeNav = 0;

  setActiveNav(i: number) {
    this.activeNav = i;
  }

  // Propiedades del modal
  isModalOpen: boolean = false;
  isBlurred: boolean = false;

  weekDays = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];

  today = new Date();
  currentMonth = this.today.getMonth();
  currentYear = this.today.getFullYear();

  monthLabel = '';
  calendarCells: (number | null)[] = []; // null = celda vacía previa
  selectedDay: number | null = null;
  selectedDayTitle = 'Today';

  agenda: AgendaItem[] = [
    {time: '08:00', label: 'Marketing', desc: '5 posts on instagram', color: 'yellow'},
    {time: '10:00', label: 'Animation', desc: 'Platform App Concept', color: 'purple'},
    {time: '11:00', label: 'Animation', desc: 'Platform Concept', color: 'blue'},
  ];
  selectedEvent: number | null = null;

  public currentTrip: TripResponse | null = null;


  constructor(private router: Router, public tService: TripService) {
    this.renderCalendar(this.currentMonth, this.currentYear);
  }

  ngOnInit() {
    this.loadTripFromLocalStorage();
  }

  private fetchTripData(id: string) {

    this.tService.getTripById(id).subscribe({
      next: (trip: TripResponse) => {
        this.currentTrip = trip;
        console.log('Viaje cargado:', this.currentTrip);
      },
      error: (e: any) => {
        console.error('Error al cargar el viaje con ID:', id, e);
      }
    });
  }

  private loadTripFromLocalStorage() {
    // intento de obtener el id del viaje seleccionado
    const tripId = localStorage.getItem('selectedTripId');

    if (tripId) {
      // se carga el viaje seleccionado mediante el id
      this.fetchTripData(tripId);


    } else {
      console.warn('No se encontró un ID de viaje en localStorage');
      this.router.navigate(['/error']);
    }
  }


  private monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  renderCalendar(month: number, year: number) {
    this.calendarCells = [];
    this.monthLabel = `${this.monthNames[month].toUpperCase()} ${year}`;

    const firstDay = new Date(year, month, 1).getDay(); // 0 = Sun
    const totalDays = new Date(year, month + 1, 0).getDate();

    for (let i = 0; i < firstDay; i++) this.calendarCells.push(null);
    for (let d = 1; d <= totalDays; d++) this.calendarCells.push(d);

    // set default selection (hoy)
    this.selectedDay = this.today.getMonth() === month && this.today.getFullYear() === year
      ? this.today.getDate()
      : 1;
    this.updateSelectedDayTitle();
  }

  isToday(day: number | null): boolean {
    if (day === null) return false;
    return (
      day === this.today.getDate() &&
      this.currentMonth === this.today.getMonth() &&
      this.currentYear === this.today.getFullYear()
    );
  }

  selectDay(day: number) {
    this.selectedDay = day;
    this.updateSelectedDayTitle();
  }

  private updateSelectedDayTitle() {
    if (this.selectedDay === null) {
      this.selectedDayTitle = 'Today';
      return;
    }
    const d = new Date(this.currentYear, this.currentMonth, this.selectedDay);
    const opts: Intl.DateTimeFormatOptions = {weekday: 'long', day: '2-digit', month: 'long'};
    this.selectedDayTitle = d.toLocaleDateString(undefined, opts);
  }

  // --- AGENDA ---
  selectEvent(i: number) {
    this.selectedEvent = i;
  }

// LÓGICA DEL MODAL:
  toggleModal(open: boolean) {
    this.isModalOpen = open;
    this.isBlurred = open; // Aplica la variable al grid
  }

  closeOnOverlay(event: MouseEvent) {
    if (event.target instanceof HTMLElement && event.target.classList.contains('modal-overlay')) {
      this.toggleModal(false);
    }
  }

  @HostListener('document:keydown.escape')
  handleEscapeKey() {
    if (this.isModalOpen) {
      this.toggleModal(false);
    }
  }


}
