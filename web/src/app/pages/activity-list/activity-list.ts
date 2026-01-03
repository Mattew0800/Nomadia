// src/app/components/activity-list/activity-list.component.ts
import { Component, EventEmitter, Output, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {ActivityService} from '../../services/Activity/activity-service';
import { ActivityResponseDTO } from '../../models/ActivityResponse';
import {Test} from '../test/test';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-activity-list',
  standalone: true,
  imports: [CommonModule, FormsModule, Test],
  templateUrl: './activity-list.html',
  styleUrls: ['./activity-list.scss']
})
export class ActivityListComponent implements OnInit {

  @Output() select = new EventEmitter<ActivityResponseDTO>();

  // estado
  loading = signal(false);
  errorMsg = signal<string | null>(null);
  activities: ActivityResponseDTO[] = [];

  // filtros UI (opcionales)
  showFilters = true;              // poné false si no querés ver el form
  fromDate?: string;               // 'YYYY-MM-DD'
  toDate?: string;
  fromTime?: string;               // 'HH:mm'
  toTime?: string;

  constructor(private activityService: ActivityService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    // Escuchamos los parámetros de la URL
    this.route.queryParams.subscribe(params => {
      const searchTerm = params['search'];
      if (searchTerm) {
        // Si hay un término de búsqueda, filtramos
        this.fetch(searchTerm);
      } else {
        // Si no, cargamos todo normal como ya hacías
        this.fetch();
      }
    });
  }

  fetch(searchTerm?: string): void {
    this.loading.set(true);
    this.errorMsg.set(null);
    this.activities = [];

    const hasFilters = this.fromDate || this.toDate || this.fromTime || this.toTime;

    const req$ = hasFilters
      ? this.activityService.listMineFiltered({
        fromDate: this.fromDate,
        toDate: this.toDate,
        fromTime: this.fromTime,
        toTime: this.toTime
      })
      : this.activityService.listMine();

    req$.subscribe({
      next: (list) => {
        let result = list ?? [];

        // --- LÓGICA DE BÚSQUEDA INTEGRADA ---
        if (searchTerm) {
          const term = searchTerm.toLowerCase();
          result = result.filter(a => a.name.toLowerCase().includes(term));
        }

        // Ordenar: fecha asc, luego nombre
        this.activities = [...result].sort((a, b) =>
          (a.date ?? '').localeCompare(b.date ?? '') || a.name.localeCompare(b.name)
        );

        // Si después de filtrar no hay nada, mostrar mensaje
        if (this.activities.length === 0) {
          this.errorMsg.set(searchTerm ? `No se encontró: "${searchTerm}"` : 'No hay actividades.');
        }

        this.loading.set(false);
      },
      error: (e) => {
        console.error(e);
        // 204 No Content también puede venir sin body
        const msg = e?.status === 204 ? 'No hay actividades.' :
          e?.error?.error || 'No se pudieron cargar las actividades.';
        this.errorMsg.set(msg);
        this.loading.set(false);
      }
    });
  }

  clearFilters(): void {
    this.fromDate = this.toDate = this.fromTime = this.toTime = undefined;
    this.fetch();
  }

  onSelect(a: ActivityResponseDTO) {
    this.select.emit(a);
  }

  // activity-list.ts
  onDelete(a: ActivityResponseDTO) {
    if (!confirm(`¿Eliminar la actividad "${a.name}"?`)) return;

    this.activityService.deleteActivity(String(a.tripId), String(a.id)).subscribe({
      next: () => {
        // sacar del array sin volver a llamar a la API
        this.activities = this.activities.filter(x => x.id !== a.id);
      },
      error: (e) => {
        console.error(e);
        const msg =
          e?.status === 403 ? 'No tenés permiso para eliminar esta actividad.' :
            e?.status === 404 ? 'La actividad no existe o no pertenece a este viaje.' :
              e?.error?.error || 'No se pudo eliminar la actividad.';
        alert(msg);
      }
    });
  }

}
