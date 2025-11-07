import {Component, HostListener, ElementRef, ViewChild, HostBinding, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterLink} from '@angular/router';
import {Test} from '../test/test';
import {TripService} from '../../services/trip-service';
import {TripResponse} from '../../models/TripResponse';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

type AgendaItem = { time: string; label: string; desc: string; color: 'yellow' | 'purple' | 'blue' };


@Component({
  selector: 'app-main-page',
  standalone: true,
  imports: [CommonModule, Test, ReactiveFormsModule, FormsModule],
  templateUrl: './main-page.html',
  styleUrls: ['./main-page.css'],
})
export class MainPage implements OnInit {

  activeNav = 0;
  startWeekOnMonday = true;

  setActiveNav(i: number) {
    this.activeNav = i;
  }

  // Propiedades del modal
  isModalOpen: boolean = false;
  isBlurred: boolean = false;

  weekDays = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];

  get weekDaysView(): string[] {
    return this.startWeekOnMonday
      ? ['MON','TUE','WED','THU','FRI','SAT','SUN']
      : this.weekDays;
  }


  today = new Date();
  currentMonth = this.today.getMonth();
  currentYear = this.today.getFullYear();

  monthLabel = '';
  calendarCells: (number | null)[] = []; // null = celda vacía previa
  selectedDay: number | null = null;
  selectedDayTitle = 'Today';

  monthNames: string[] = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];
  years: number[] = [];

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
    this.initYears();
  }

  private fetchTripData(id: string) {
    this.tService.getTripById(id).subscribe({
      next: (trip: TripResponse) => {
        this.currentTrip = trip;
        // Posiciono automáticamente calendario en la fecha de inicio del viaje
        const start = this.getTripStartDate(trip); // lee trip.startDate (string)
        if (start) this.goToDate(start);
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


  renderCalendar(month: number, year: number) {
    this.calendarCells = [];

    const raw = new Date(year, month, 1).getDay();

    const firstDay = this.startWeekOnMonday ? (raw + 6) % 7 : raw;
    const totalDays = new Date(year, month + 1, 0).getDate();

    for (let i = 0; i < firstDay; i++) this.calendarCells.push(null);
    for (let d = 1; d <= totalDays; d++) this.calendarCells.push(d);

    this.monthLabel = `${this.monthNames[month].toUpperCase()} ${year}`;


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

  private initYears() {
    this.years=[];
    const start = this.today.getFullYear() - 10;
    const end = this.today.getFullYear() + 10;
    for (let y = start; y <= end; y++) this.years.push(y);
  }

  onMonthYearChange() {
    // se llama desde los <select> con [(ngModel)]
    this.renderCalendar(this.currentMonth, this.currentYear);
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

  /** Parsea 'YYYY-MM-DD' como fecha local (sin shift por timezone). Si viene ISO con tiempo/Z, usa Date normal. */
  private parseTripDate(raw: string | null | undefined): Date | null {
    if (!raw) return null;

    // 'YYYY-MM-DD'
    const ymd = /^(\d{4})-(\d{2})-(\d{2})$/;
    const m = ymd.exec(raw);
    if (m) {
      const y = Number(m[1]), mo = Number(m[2]) - 1, d = Number(m[3]);
      return new Date(y, mo, d);
    }

    // ISO completa
    const d = new Date(raw);
    return isNaN(d.getTime()) ? null : d;
  }

  /** Lee startDate del trip (TripResponse o TripCreate) y devuelve Date */
  private getTripStartDate(trip: TripResponse | { startDate?: string }): Date | null {
    const raw = (trip as any)?.startDate ?? null;
    return this.parseTripDate(raw);
  }

  /** Si el año no está en el rango del selector, lo extiendo para que aparezca */
  private ensureYearInRange(year: number) {
    if (!this.years || this.years.length === 0) return;
    const min = Math.min(...this.years);
    const max = Math.max(...this.years);
    if (year < min || year > max) {
      const start = Math.min(year, min);
      const end = Math.max(year, max);
      this.years = Array.from({length: end - start + 1}, (_, i) => start + i);
    }
  }

  /** Posiciona el calendario en la fecha indicada y selecciona ese día */
  private goToDate(date: Date) {
    const m = date.getMonth();
    const y = date.getFullYear();
    const d = date.getDate();

    // me aseguro que el año exista en el selector
    this.ensureYearInRange(y);

    // renderizo mes/año del viaje
    this.currentMonth = m;
    this.currentYear = y;
    this.renderCalendar(this.currentMonth, this.currentYear);

    // selecciono el día de inicio
    this.selectedDay = d;
    this.updateSelectedDayTitle();
  }
}



