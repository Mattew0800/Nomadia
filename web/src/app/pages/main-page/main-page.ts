import {Component, HostListener, ElementRef, ViewChild, HostBinding, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router, RouterLink} from '@angular/router';
import {Test} from '../test/test';
import {TripService} from '../../services/Trip/trip-service';
import {TripResponse} from '../../models/TripResponse';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule, ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {ActivityService} from '../../services/Activity/activity-service';
import {User} from '../../models/User';
import {TravelerResponse} from '../../models/TravelerResponse';
import {ActivityResponseDTO} from '../../models/ActivityResponse';
import {ActivityCreateDTO} from '../../models/ActivityCreate';
import {DomSanitizer, SafeUrl} from '@angular/platform-browser';

type AgendaItem = {
  time: string;
  label: string;
  desc: string;
  color: 'yellow' | 'purple' | 'blue' ;
  tripId: string;
  activityId: string;

  date: string;          // 'YYYY-MM-DD'
  cost: number;
  startTimeRaw?: string; // 'HH:mm'
  endTimeRaw?: string;
};


@Component({
  selector: 'app-main-page',
  standalone: true,
  imports: [CommonModule, Test, ReactiveFormsModule, FormsModule],
  templateUrl: './main-page.html',
  styleUrls: ['./main-page.scss'],
})

export class MainPage implements OnInit {

  isLoading: boolean = true;
  activeNav = 0;
  startWeekOnMonday = true;

  msgInviteOk?: string;
  msgInviteError?: string;

  msgCreateOk?: string;
  msgCreateError?: string;

  msgEditOk?: string;
  msgEditError?: string;

  setActiveNav(i: number) {
    this.activeNav = i;
  }
  emptyActivitiesList: boolean = false;

  // Propiedades del modal
  isModalOpen: boolean = false;
  isBlurred: boolean = false;

  isCreateOpen = false;
  createForm!: FormGroup;

  editForm!: FormGroup;
  isEditPanelOpen = false; // panel deslizante visible/oculto
  private editingIndex: number | null = null;
  private editingActivityId: string | null = null;
  private editingTripId: string | null = null;

  // 🆕: locks por campo: arrancan bloqueados (read-only)
  locks: Record<'name' | 'date' | 'startTime' | 'endTime' | 'cost' | 'description', boolean> = {
    name: true, date: true, startTime: true, endTime: true, cost: true, description: true
  };

  // 🆕: refs a inputs para enfocar al desbloquear
  @ViewChild('nameInput') nameInput!: ElementRef<HTMLInputElement>;
  @ViewChild('dateInput') dateInput!: ElementRef<HTMLInputElement>;
  @ViewChild('startInput') startInput!: ElementRef<HTMLInputElement>;
  @ViewChild('endInput') endInput!: ElementRef<HTMLInputElement>;
  @ViewChild('costInput') costInput!: ElementRef<HTMLInputElement>;
  @ViewChild('descInput') descInput!: ElementRef<HTMLInputElement>;

  weekDays = ['DOM', 'LUN', 'MAR', 'MIE', 'JUE', 'VIE', 'SAB'];

  get weekDaysView(): string[] {
    return this.startWeekOnMonday
      ? ['LUN', 'MAR', 'MIE', 'JUE', 'VIE', 'SAB', 'DOM']
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
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ];
  years: number[] = [];

  agenda: AgendaItem[] = [

  ];
  selectedEvent: number | null = null;

  public currentTrip: TripResponse | null = null;

  // mapa para trackear imágenes que fallaron al cargar (clave: email o id)
  avatarLoadError: Record<string, boolean> = {};

  constructor(private router: Router, public tService: TripService, private fb: FormBuilder, private activityApi: ActivityService, private sanitizer: DomSanitizer) {
    this.renderCalendar(this.currentMonth, this.currentYear);
  }

  /** Normaliza photoUrl: acepta http(s), rutas relativas, data: y base64 puro (lo convierte en data:png). Retorna SafeUrl para evitar sanitización removals */
  getSafePhotoUrl(photo?: string): SafeUrl | null {
    if (!photo) return null;
    const trimmed = photo.trim();
    if (trimmed.startsWith('data:') || trimmed.startsWith('http') || trimmed.startsWith('/')) {
      return this.sanitizer.bypassSecurityTrustUrl(trimmed);
    }

    // Si parece base64 (solo chars base64 y bastante largo), añadimos prefijo data:image/png;base64,
    const base64Like = /^[A-Za-z0-9+/=\s]+$/.test(trimmed) && trimmed.length > 100;
    if (base64Like) {
      return this.sanitizer.bypassSecurityTrustUrl('data:image/png;base64,' + trimmed.replace(/\s+/g, ''));
    }

    // Fallback: devolver tal cual (sanitizado)
    return this.sanitizer.bypassSecurityTrustUrl(trimmed);
  }

  onAvatarError(event: Event, user: any) {
    const key = user?.email || user?.id || user?.name || Math.random().toString(36).slice(2,7);
    this.avatarLoadError[key] = true;
    // esconder la imagen fallida si aún está en el DOM
    const img = event.target as HTMLImageElement | null;
    if (img) img.style.display = 'none';
  }

  avatarKey(user: any): string {
    return (user?.email || user?.id || user?.name || '').toString();
  }

  ngOnInit() {
    this.loadTripFromLocalStorage();
    this.initYears();
    this.initCreateForm();
    this.initEditForm();
  }

  private fetchTripData(id: string) {
    this.isLoading = true;
    this.tService.getTripById(id).subscribe({
      next: (trip: TripResponse) => {
        this.currentTrip = trip;
        const start = this.getTripStartDate(trip);
        if (start) this.goToDate(start);
        this.loadAgendaForSelectedDay();

        console.log('Viaje cargado:', this.currentTrip);
        this.isLoading = false;
      },
      error: (e: any) => {
        console.error('Error al cargar el viaje con ID:', id, e);
        localStorage.removeItem('selectedTripId');
        this.isLoading = false;
        this.router.navigate(['/tripList']);
      }
    });
  }

  private loadTripFromLocalStorage() {

    const tripId = localStorage.getItem('selectedTripId');

    if (tripId) {
      this.fetchTripData(tripId);
      this.getTravelers(tripId);

    } else {
      console.warn('No se encontró un ID de viaje en localStorage');
      this.router.navigate(['/tripList']);
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

  onMonthYearChange() {
    // se llama desde los <select> con [(ngModel)]
    this.renderCalendar(this.currentMonth, this.currentYear);
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
    this.loadAgendaForSelectedDay();

    if (this.isEditPanelOpen) {
      this.closeEditPanel();
    }
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
    this.years = [];
    const start = this.today.getFullYear() - 10;
    const end = this.today.getFullYear() + 10;
    for (let y = start; y <= end; y++) this.years.push(y);
  }


  selectEvent(i: number) {
    this.selectedEvent = i;
  }

  private loadAgendaForSelectedDay() {
    if (!this.currentTrip || this.selectedDay == null) return;

    this.activityApi.listByTrip(Number(this.currentTrip.id)).subscribe({
      next: (list) => {
        const y = this.currentYear, m = this.currentMonth + 1, d = this.selectedDay!;
        const pad = (n: number) => String(n).padStart(2, '0');
        const selectedStr = `${y}-${pad(m)}-${pad(d)}`;

        if (!list || list.length === 0) {
          this.emptyActivitiesList = true;
        } else {
          this.emptyActivitiesList = false;
        }

        const sameDay = list.filter(a => (a.date ?? '').startsWith(selectedStr));

        this.agenda = sameDay
          .sort((a, b) => (a.startTime || '').localeCompare(b.startTime || ''))
          .map(a => ({
            time: (a.startTime || '').slice(0, 5),
            label: a.name,
            desc: a.description,
            color: 'blue' as const,
            tripId: String(this.currentTrip!.id),
            activityId: String(a.id),

            date: a.date ?? selectedStr,
            cost: Number(a.cost ?? 0),
            startTimeRaw: a.startTime ?? '09:00',
            endTimeRaw: a.endTime ?? '10:00'
          }));
      },
      error: (e) => console.error(e)
    });
  }

  // LÓGICA DEL MODAL:
  toggleModal(open: boolean) {
    this.isModalOpen = open;
    this.isBlurred = open; // Aplica la variable al grid
  }

  closeOnOverlay(event: MouseEvent) {
    if (event.target instanceof HTMLElement && event.target.classList.contains('modal-overlay')) {
      this.isModalOpen = false;
      this.isCreateOpen = false;
      this.isBlurred = false;

      this.isEditPanelOpen = false;
    }
  }

  @HostListener('document:keydown.escape')
  handleEscapeKey() {
    if (this.isModalOpen || this.isCreateOpen || this.isEditPanelOpen) {
      this.isModalOpen = false;
      this.isCreateOpen = false;
      this.isEditPanelOpen = false;
      this.isBlurred = false;
    }
  }

  private initCreateForm() {
    this.createForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120), this.notOnlyWhitespaceValidator()]],
      date: ['', Validators.required],          // YYYY-MM-DD
      description: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(2000)]],
      cost: [0, [Validators.required, Validators.min(0)]],
      startTime: ['09:00', Validators.required], // HH:mm
      endTime: ['10:00', Validators.required],   // HH:mm
    },{ validators: this.timeRangeValidator() });
  }

  openCreateActivity() {
    const y = this.currentYear;
    const m = this.currentMonth + 1;
    const d = this.selectedDay ?? 1;
    const pad = (n: number) => String(n).padStart(2, '0');
    const yyyyMMdd = `${y}-${pad(m)}-${pad(d)}`;

    this.createForm.reset({
      name: '',
      date: yyyyMMdd,
      description: '',
      startTime: '09:00',
      endTime: '10:00'
    });

    this.isCreateOpen = true;
    this.isBlurred = true; // mismo blur que el otro modal
  }

  closeCreateActivity() {
    this.isCreateOpen = false;
    this.isBlurred = false;
    this.msgCreateOk = '';
    this.msgCreateError = '';
  }

  submitCreateActivity() {
    if (!this.currentTrip) return;

    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const v = this.createForm.value;

    const payload = {
      tripId: (this.currentTrip as any).id,
      name: v.name,
      date: v.date, // 'YYYY-MM-DD'
      description: v.description,
      cost: Number(v.cost),
      startTime: v.startTime, // 'HH:mm'
      endTime: v.endTime,     // 'HH:mm'
      tripStartDate: (this.currentTrip as any)?.startDate ?? undefined,
      tripEndDate: (this.currentTrip as any)?.endDate ?? undefined,
    };

    this.activityApi.create(payload).subscribe({
      next: () => {
        this.loadAgendaForSelectedDay();
        this.msgCreateOk="Actividad creada con exito."
        this.msgCreateError = '';
        this.closeCreateActivity();
        this.activityApi.activities = [...this.activityApi.activities, payload];
        if(this.activityApi.activities.length > 0){
          this.emptyActivitiesList = false;
        }

      },
      error: (e) => {
        console.error(e);
        this.msgCreateOk = '';
        this.msgCreateError = e.error;

      }

    });

  }


  /** se parsea 'YYYY-MM-DD' como fecha local (sin shift por timezone). Si viene ISO con tiempo/Z, usa Date normal. */
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

  /** se lee startDate del trip (TripResponse o TripCreate) y devuelve Date */
  private getTripStartDate(trip: TripResponse | { startDate?: string }): Date | null {
    const raw = (trip as any)?.startDate ?? null;
    return this.parseTripDate(raw);
  }

  private getTripEndDate(trip: TripResponse | { endDate?: string }): Date | null {
    const raw = (trip as any)?.endDate ?? null;
    return this.parseTripDate(raw);
  }

  private stripTime(d: Date): Date {
    return new Date(d.getFullYear(), d.getMonth(), d.getDate());
  }

  private dateForCell(day: number): Date {
    return new Date(this.currentYear, this.currentMonth, day);
  }

  private getTripRange(): { start: Date | null; end: Date | null } {
    if (!this.currentTrip) return { start: null, end: null };
    const s = this.getTripStartDate(this.currentTrip);
    const e = this.getTripEndDate(this.currentTrip);
    if (!s || !e) return { start: null, end: null };
    const start = this.stripTime(s);
    const end = this.stripTime(e);
    return start <= end ? { start, end } : { start: end, end: start };
  }

  isInTripRange(day: number | null): boolean {
    if (day === null || !this.currentTrip) return false;
    const { start, end } = this.getTripRange();
    if (!start || !end) return false;
    const d = this.stripTime(this.dateForCell(day));
    return d >= start && d <= end;
  }

  isRangeStart(day: number | null): boolean {
    if (day === null || !this.currentTrip) return false;
    const { start } = this.getTripRange();
    if (!start) return false;
    const d = this.stripTime(this.dateForCell(day));
    return d.getTime() === start.getTime();
  }

  isRangeEnd(day: number | null): boolean {
    if (day === null || !this.currentTrip) return false;
    const { end } = this.getTripRange();
    if (!end) return false;
    const d = this.stripTime(this.dateForCell(day));
    return d.getTime() === end.getTime();
  }

  public get costControl() {
    return this.createForm.get('cost')!;
  }

  notOnlyWhitespaceValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;

      // Si es nulo, undefined o cadena vacía → válido (campo opcional)
      if (value == null || value === '') {
        return null;
      }

      // Si es string y después de quitar espacios queda vacío → error
      if (typeof value === 'string' && value.trim().length === 0) {
        return { onlyWhitespace: true };
      }

      return null;
    };
  }

  public get editCostControl() {
    return this.editForm.get('cost')!;
  }

  /** si el año no está en el rango del selector, lo extiendo para que aparezca */
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

  /** se posiciona el calendario en la fecha indicada y selecciona ese día */
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

  addUser(tripId: string,email: string){

    this.tService.addUser(tripId,email).subscribe({
      next: (traveler: TravelerResponse) => {
        this.tService.users = [...this.tService.users, traveler];
        this.msgInviteOk = "Usuario invitado con exito.";
        this.msgInviteError = "";

      },
      error: (e: any) => {
        console.log(e);

        this.msgInviteOk = "";
        this.msgInviteError = e.error;

      }
    })
  }

  removeMember(email: string) {
    if (!this.currentTrip) {
      console.error('No hay viaje actual seleccionado');
      return;
    }

    const tripId = String(this.currentTrip.id);

    this.tService.removeUser(tripId, email).subscribe({
      next: (response) => {
        console.log('Usuario eliminado exitosamente:', response);
        // Actualizar la lista local de usuarios
        this.tService.users = this.tService.users.filter(user => user.email !== email);
        this.msgInviteOk = "Usuario eliminado con éxito.";
        this.msgInviteError = "";
      },
      error: (e: any) => {
        console.error('Error completo al eliminar usuario:', e);
        this.msgInviteOk = "";

        // Manejar diferentes tipos de errores
        if (e.error && typeof e.error === 'string') {
          this.msgInviteError = e.error;
        } else if (e.error && e.error.message) {
          this.msgInviteError = e.error.message;
        } else if (e.message) {
          this.msgInviteError = e.message;
        } else if (e.status === 0) {
          this.msgInviteError = "No se pudo conectar con el servidor.";
        } else if (e.status) {
          this.msgInviteError = `Error ${e.status}: ${e.statusText || 'Error al eliminar el usuario'}`;
        } else {
          this.msgInviteError = "Error desconocido al eliminar el usuario.";
        }
      }
    });
  }

  deleteEvent(index: number) {
    const eventToDelete = this.agenda[index];

    if (!eventToDelete || !eventToDelete.tripId || !eventToDelete.activityId) {
      console.error('IDs de viaje o actividad faltantes en el evento.');
      return;
    }

    const tripId = eventToDelete.tripId;
    const activityId = eventToDelete.activityId;

    this.activityApi.deleteActivity(tripId, activityId).subscribe({
      next: () => {
        this.agenda.splice(index, 1);
        this.activityApi.activities.splice(index, 1);

        if(this.activityApi.activities.length == 0){
          this.emptyActivitiesList = true;
        }

        if (this.selectedEvent !== null) {
          if (this.selectedEvent === index) {
            this.selectedEvent = null;
          } else if (this.selectedEvent > index) {
            this.selectedEvent--;
          }
        }

        if (this.editingIndex === index) {
          this.closeEditPanel();
        }



        console.log(`Actividad ${activityId} borrada del servidor y de la UI.`);

      },
      error: (err: any) => {
        console.error('Error al borrar la actividad del servidor:', err);
      }
    });
  }

  private initEditForm() {
    this.editForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120), this.notOnlyWhitespaceValidator()]],
      date: ['', Validators.required],          // 'YYYY-MM-DD'
      description: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(2000), this.notOnlyWhitespaceValidator()]],
      cost: [0, [Validators.required, Validators.min(0)]],
      startTime: ['09:00', Validators.required], // 'HH:mm'
      endTime: ['10:00', Validators.required],   // 'HH:mm'
    }, { validators: this.timeRangeValidator() });
  }

  /** Abre el panel y precarga datos de la actividad clickeada */
  openEditPanel(ev: AgendaItem, index: number) {
    this.editingIndex = index;
    this.editingActivityId = ev.activityId;
    this.editingTripId = ev.tripId;

    this.editForm.reset({
      name: ev.label,
      date: ev.date,
      description: ev.desc,
      cost: ev.cost ?? 0,
      startTime: ev.startTimeRaw ?? ev.time, // ev.time ya viene 'HH:mm'
      endTime: ev.endTimeRaw ?? '10:00'
    });

    // todos los campos arrancan bloqueados; se desbloquean clickeando cada .field
    this.locks = { name: true, date: true, startTime: true, endTime: true, cost: true, description: true };

    this.msgEditOk = '';
    this.msgEditError = '';

    this.isEditPanelOpen = true;
    this.isBlurred = false; // opcional: reaprovechamos blur general
  }

  /** Cierra panel y limpia estado */
  closeEditPanel() {
    this.isEditPanelOpen = false;
    this.isBlurred = false;
    this.msgEditOk = '';
    this.msgEditError = '';
    this.editingIndex = null;
    this.editingActivityId = null;
    this.editingTripId = null;
  }

  /** Desbloquea un campo y le da foco al input correspondiente */
  unlock(field: 'name' | 'date' | 'startTime' | 'endTime' | 'cost' | 'description') {
    if (!this.locks[field]) return;
    this.locks[field] = false;

    setTimeout(() => {
      switch (field) {
        case 'name': this.nameInput?.nativeElement?.focus(); break;
        case 'date': this.dateInput?.nativeElement?.focus(); break;
        case 'startTime': this.startInput?.nativeElement?.focus(); break;
        case 'endTime': this.endInput?.nativeElement?.focus(); break;
        case 'cost': this.costInput?.nativeElement?.focus(); break;
        case 'description': this.descInput?.nativeElement?.focus(); break;
      }
    });
  }

  /** Envía actualización al backend y refresca la agenda */
  submitEditActivity() {
    if (this.editForm.invalid || !this.editingActivityId || !this.editingTripId) {
      this.editForm.markAllAsTouched();
      return;
    }

    const v = this.editForm.value;
    const payload = {
      activityId: this.editingActivityId,
      tripId: this.editingTripId,
      name: v.name,
      date: v.date,
      description: v.description,
      cost: Number(v.cost),
      startTime: v.startTime,
      endTime: v.endTime
    };

    this.activityApi.updateActivity(this.editingTripId, this.editingActivityId, payload).subscribe({
      next: () => {
        this.loadAgendaForSelectedDay(); // recarga el día currentTrip
        this.msgEditOk = 'Actividad actualizada con éxito.';
        this.msgEditError = '';
        this.closeEditPanel();
      },
      error: (e) => {
        console.error(e);
        this.msgEditOk = '';
        this.msgEditError = e.error?.error ?? 'Error al actualizar la actividad.';
      }
    });
  }

  public get nameControl() {
    return this.createForm.get('name')!;
  }

  public get descriptionControl() {
    return this.createForm.get('description')!;
  }

  public get editNameControl() {
    return this.editForm.get('name')!;
  }

  public get editDescriptionControl() {
    return this.editForm.get('description')!;
  }

  getTravelers(tripId: string) {
    return this.tService.getUsers(tripId).subscribe({
      next: (users: TravelerResponse[]) => {
        console.log('Travelers received:', users);
        // Loguear photoUrl para depuración
        users.forEach(u => console.log('traveler', u.email || u.id, 'photoUrl:', u.photoUrl ? (u.photoUrl.length > 200 ? u.photoUrl.slice(0, 200) + '... (len ' + u.photoUrl.length + ')' : u.photoUrl) : '(no photo)'));
        this.tService.users = users;
      },
      error: (e) => {
        console.log(e)
      }
    })
  }

  timeRangeValidator(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const start = group.get('startTime')?.value;
      const end = group.get('endTime')?.value;

      // Si alguno está vacío, no validamos (los required ya se encargan)
      if (!start || !end) {
        return null;
      }

      // Si son iguales, devolvemos error
      if (start === end) {
        return { timeEqual: true };
      }

      return null;
    };
  }



  protected readonly String = String;

  /** Devuelve 'AM' o 'PM' según una hora en formato 'HH:mm' */
  getAmPm(time: string | null | undefined): string {
    if (!time) return '';
    const hour = parseInt(time.split(':')[0], 10);
    if (isNaN(hour)) return '';
    return hour < 12 ? 'AM' : 'PM';
  }
}
