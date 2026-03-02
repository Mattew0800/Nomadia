import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AuthService} from '../Auth/auth-service';
import {Observable} from 'rxjs';
import {CreateExpenseDTO} from '../../models/CreateExpenseDTO';
import {ExpenseResponseDTO} from '../../models/ExpenseResponseDTO';
import {ExpenseUpdateDTO} from '../../models/ExpenseUpdateDTO';
import {UserBalanceDTO} from '../../models/UserBalanceDTO';
import {DebtDTO} from '../../models/DebtDTO';
import {UserDebtProgressDTO} from '../../models/UserDebtProgressDTO';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ExpenseService {

  private API_URL = `${environment.apiUrl}/nomadia/expense`;

  constructor(public http: HttpClient, public authService: AuthService) {}

  createExpense(expense: CreateExpenseDTO): Observable<ExpenseResponseDTO> {
    return this.http.post<ExpenseResponseDTO>(
      `${this.API_URL}/create`,
      expense,
      { headers: this.authService.authHeaders() }
    );
  }

  updateExpense(body: ExpenseUpdateDTO): Observable<ExpenseResponseDTO> {
    return this.http.put<ExpenseResponseDTO>(
      `${this.API_URL}/update`,
      body,
      { headers: this.authService.authHeaders() }
    );
  }

  deleteExpense(expenseId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.API_URL}/delete`,
      {
        body: { expenseId },
        headers: this.authService.authHeaders()
      }
    );
  }

  getExpensesByTrip(tripId: number): Observable<ExpenseResponseDTO[]> {
    return this.http.post<ExpenseResponseDTO[]>(
      `${this.API_URL}/by-trip`,
      { tripId },
      { headers: this.authService.authHeaders() }
    );
  }

  getExpensesByActivity(activityId: number): Observable<ExpenseResponseDTO[]> {
    return this.http.post<ExpenseResponseDTO[]>(
      `${this.API_URL}/by-activity`,
      { activityId },
      { headers: this.authService.authHeaders() }
    );
  }

  getExpenseById(expenseId: number): Observable<ExpenseResponseDTO> {
    return this.http.post<ExpenseResponseDTO>(
      `${this.API_URL}/get`,
      { expenseId },
      { headers: this.authService.authHeaders() }
    );
  }

  getTripBalance(tripId: number): Observable<UserBalanceDTO[]> {
    return this.http.post<UserBalanceDTO[]>(
      `${this.API_URL}/balance`,
      { tripId },
      { headers: this.authService.authHeaders() }
    );
  }

  getTripDebts(tripId: number): Observable<UserDebtProgressDTO> {
    return this.http.post<UserDebtProgressDTO>(
      `${environment.apiUrl}/nomadia/trip/debts`,
      { tripId },
      { headers: this.authService.authHeaders() }
    );
  }


}
