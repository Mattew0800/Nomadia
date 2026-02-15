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
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUserId = user.id ? Number(user.id) : 0;

        this.route.queryParams.subscribe(params => {
          this.tripId = params['tripId'];
          if (this.tripId) {
            this.loadBalanceData();
          } else {
            console.error('No tripId provided');
            this.isLoading = false;
          }
        });
      },
      error: (err) => {
        console.error('Error getting current user:', err);
        this.isLoading = false;
      }
    });
  }

  loadBalanceData() {
    this.isLoading = true;

    this.tripService.getUsers(this.tripId).subscribe({
      next: (travelers) => {
        travelers.forEach(t => {
          this.travelers.set(Number(t.id), t);
        });
        this.loadOtherData();
      },
      error: (error) => {
        console.error('Error loading travelers:', error);
        this.isLoading = false;
      }
    });
  }

  private loadOtherData() {
    let loadedCount = 0;
    const totalRequests = 3;

    const checkIfAllLoaded = () => {
      loadedCount++;
      if (loadedCount >= totalRequests) {
        this.isLoading = false;
      }
    };

    this.expenseService.getTripBalance(Number(this.tripId)).subscribe({
      next: (balances) => {
        this.balances = balances;

        this.calculateAverageCost();
        checkIfAllLoaded();
      },
      error: (error) => {
        console.error('Error loading balances:', error);
        checkIfAllLoaded();
      }
    });

    this.expenseService.getTripDebts(Number(this.tripId)).subscribe({
      next: (debts) => {
        this.debts = debts;
        checkIfAllLoaded();
      },
      error: (error) => {
        console.error('Error loading debts:', error);
        checkIfAllLoaded();
      }
    });

    this.expenseService.getTotalTripCost(Number(this.tripId)).subscribe({
      next: (total) => {
        this.totalCost = total;
        this.calculateAverageCost();
        checkIfAllLoaded();
      },
      error: (error) => {
        console.error('Error loading total cost:', error);
        checkIfAllLoaded();
      }
    });
  }

  calculateAverageCost() {
    if (this.balances.length > 0 && this.totalCost > 0) {
      this.averageCost = this.totalCost / this.balances.length;
    }
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

  abs(value: number): number {
    return Math.abs(value);
  }


  trackByIndex(index: number): number {
    return index;
  }

  // Métodos para el gráfico de torta
  getPaidPercentage(): number {
    if (this.debts.length === 0) return 100;
    // Por simplicidad, asumimos que todas las deudas están pendientes
    // En una implementación real, tendrías un campo "isPaid" en DebtDTO
    return 0;
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
}
