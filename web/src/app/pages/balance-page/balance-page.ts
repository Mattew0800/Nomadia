import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Test } from '../test/test';
import { ExpenseService } from '../../services/Expenses/expense-service';
import { TripService } from '../../services/Trip/trip-service';
import { UserService } from '../../services/User/user-service';
import { UserBalanceDTO } from '../../models/UserBalanceDTO';
import { DebtDTO } from '../../models/DebtDTO';
import { TravelerResponse } from '../../models/TravelerResponse';

@Component({
  selector: 'app-balance-page',
  standalone: true,
  imports: [Test, CommonModule],
  templateUrl: './balance-page.html',
  styleUrl: './balance-page.scss',
})
export class BalancePage implements OnInit {
  math = Math;
  tripId: string = '';
  balances: UserBalanceDTO[] = [];
  debts: DebtDTO[] = [];
  debtProgress: { totalDebts: number, settledDebts: number, pendingDebts: number, percentage: number } | null = null;
  travelers: Map<number, TravelerResponse> = new Map();
  totalCost: number = 0;
  averageCost: number = 0;
  isLoading: boolean = true;
  currentUserId: number = 0;

  constructor(
    private route: ActivatedRoute,
    private expenseService: ExpenseService,
    private tripService: TripService,
    private userService: UserService
  ) {}

  ngOnInit() {
    console.log('[BalancePage] ngOnInit - obteniendo usuario actual');
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUserId = user.id ? Number(user.id) : 0;
        console.log('[BalancePage] Usuario actual:', this.currentUserId);

        this.route.queryParams.subscribe(params => {
          this.tripId = params['tripId'];
          console.log('[BalancePage] tripId desde queryParams:', this.tripId);
          if (this.tripId) {
            this.loadBalanceData();
          } else {
            console.error('[BalancePage] No se recibió tripId en los queryParams');
            this.isLoading = false;
          }
        });
      },
      error: (err) => {
        console.error('[BalancePage] Error al obtener usuario actual:', err);
        this.isLoading = false;
      }
    });
  }

  loadBalanceData() {
    this.isLoading = true;
    console.log('[BalancePage] Cargando viajeros del viaje:', this.tripId);

    this.tripService.getUsers(this.tripId).subscribe({
      next: (travelers) => {
        console.log('[BalancePage] Viajeros recibidos:', travelers);
        travelers.forEach(t => {
          this.travelers.set(Number(t.id), t);
        });
        this.loadOtherData();
      },
      error: (error) => {
        console.error('[BalancePage] Error al cargar viajeros:', error);
        this.isLoading = false;
      }
    });
  }

  private loadOtherData() {
    // Solo hay 2 requests: balance y debts
    let loadedCount = 0;
    const totalRequests = 2;

    const checkIfAllLoaded = () => {
      loadedCount++;
      console.log(`[BalancePage] Requests completadas: ${loadedCount}/${totalRequests}`);
      if (loadedCount >= totalRequests) {
        console.log('[BalancePage] Todos los datos cargados. isLoading = false');
        this.isLoading = false;
      }
    };

    console.log('[BalancePage] Cargando balance del viaje:', this.tripId);
    this.expenseService.getTripBalance(Number(this.tripId)).subscribe({
      next: (balances) => {
        console.log('[BalancePage] Balance recibido:', balances);
        this.balances = balances;
        checkIfAllLoaded();
      },
      error: (error) => {
        console.error('[BalancePage] Error al cargar balance:', error);
        checkIfAllLoaded();
      }
    });

    console.log('[BalancePage] Cargando deudas del viaje:', this.tripId);
    this.expenseService.getTripDebts(Number(this.tripId)).subscribe({
      next: (debtProgressData) => {
        console.log('[BalancePage] Deudas recibidas:', debtProgressData);
        this.debts = debtProgressData.debts;
        this.debtProgress = {
          totalDebts: debtProgressData.totalDebts,
          settledDebts: debtProgressData.settledDebts,
          pendingDebts: debtProgressData.pendingDebts,
          percentage: debtProgressData.percentage
        };
        checkIfAllLoaded();
      },
      error: (error) => {
        console.error('[BalancePage] Error al cargar deudas:', error);
        checkIfAllLoaded();
      }
    });


  }


  getTraveler(userId: number): TravelerResponse | undefined {
    return this.travelers.get(userId);
  }

  getUserBalance(userId: number): UserBalanceDTO | undefined {
    return this.balances.find(b => b.userId === userId);
  }

  formatCurrency(amount: number): string {
    const absAmount = Math.abs(amount);
    const formatted = new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
      minimumFractionDigits: 2
    }).format(absAmount);

    return amount < 0 ? `-${formatted}` : formatted;
  }

  getBalanceClass(balance: number): string {
    if (balance > 0) return 'positive';
    if (balance < 0) return 'negative';
    return 'neutral';
  }


  trackByIndex(index: number): number {
    return index;
  }

  // Métodos para el gráfico de torta
  getPaidPercentage(): number {
    if (!this.debtProgress) return 100;
    return Math.round(this.debtProgress.percentage);
  }

  getPendingPercentage(): number {
    return 100 - this.getPaidPercentage();
  }

  getPaidPercentageCircle(): string {
    const percentage = this.getPaidPercentage();
    const circumference = 2 * Math.PI * 70; // radio = 70
    const filled = (percentage / 100) * circumference;
    const empty = circumference - filled;
    return `${filled} ${empty}`;
  }

  getCircleCircumference(): string {
    const circumference = 2 * Math.PI * 70; // radio = 70
    return `${circumference} 0`;
  }

  toggleDebtStatus(index: number) {
    if (!this.debts[index]) {
      return;
    }

    const debt = this.debts[index];

    // Solo permitir marcar como saldado si el usuario actual es el deudor
    // (el que debe pagar)
    if (debt.debtorId !== this.currentUserId) {
      console.error('Solo el deudor puede marcar la deuda como saldada');
      return;
    }

    // Llamar al backend para registrar el pago
    this.tripService.settleDebt(Number(this.tripId), debt.creditorId).subscribe({
      next: (response) => {
        console.log('Deuda saldada exitosamente:', response);
        // Recargar los datos para obtener el estado actualizado
        this.loadBalanceData();
      },
      error: (error) => {
        console.error('Error completo al saldar la deuda:', error);
        const errorMessage = error.error?.message || error.message || 'Error desconocido';
        alert(`Error al saldar la deuda: ${errorMessage}`);
      }
    });
  }

  get pendingDebts(): DebtDTO[] {
    // Todas las deudas en la lista son pendientes (no saldadas)
    return this.debts || [];
  }

  get settledDebts(): DebtDTO[] {
    // Las deudas saldadas no se envían en la lista, solo se cuentan en debtProgress
    return [];
  }
}
