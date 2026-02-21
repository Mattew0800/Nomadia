import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Test } from '../test/test';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray, AbstractControl, ValidationErrors,
  ValidatorFn
} from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TravelerResponse } from '../../models/TravelerResponse';
import { ActivityResponseDTO } from '../../models/ActivityResponse';
import { CreateExpenseDTO } from '../../models/CreateExpenseDTO';
import { ExpenseUpdateDTO } from '../../models/ExpenseUpdateDTO';
import { ExpenseResponseDTO } from '../../models/ExpenseResponseDTO';
import { ExpenseService } from '../../services/Expenses/expense-service';
import { TripService } from '../../services/Trip/trip-service';
import { ActivityService } from '../../services/Activity/activity-service';
import { UserService } from '../../services/User/user-service';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-expenses-page',
  imports: [Test, CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './expenses-page.html',
  styleUrl: './expenses-page.scss',
})
export class ExpensesPage implements OnInit {

  readonly MAX_TOTAL_AMOUNT = 999999999.99; // 999.999.999,99


  expenseForm!: FormGroup;

  tripMembers: TravelerResponse[] = [];
  activities: ActivityResponseDTO[] = [];

  tripId: string = '';
  isLoading: boolean = true;
  noTripSelected: boolean = false;

  expenses: any[] = [];
  filteredExpenses: any[] = [];
  allExpenses: any[] = [];

  divisionType: 'equal' | 'custom' = 'equal';
  editingSplits = new Set<number>();
  editingPayers = new Set<number>();
  editingTotalAmount = false;

  // Estados de edición para filtros
  editingFilterMinAmount = false;
  editingFilterMaxAmount = false;

  showForm: boolean = false;
  showFilters: boolean = false;
  selectedExpenseId: number | null = null;
  selectedExpense: any = null;
  filterAmountRangeInvalid: boolean = false;

  showDeleteModal: boolean = false;
  expenseToDelete: number | null = null;

  filterActivityId: number | null = null;
  filterMinAmount: number | null = null;
  filterMaxAmount: number | null = null;

  currentUserId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private expenseService: ExpenseService,
    private tripService: TripService,
    private activityService: ActivityService,
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    const tripId = localStorage.getItem('selectedTripId') || '';
    if (!tripId || tripId === 'null' || tripId === 'undefined') {
      this.noTripSelected = true;
      this.isLoading = false;
    }
  }

  ngOnInit(): void {
    this.tripId = localStorage.getItem('selectedTripId') || '';


    if (!this.tripId || this.tripId === 'null' || this.tripId === 'undefined') {

      if (this.tripId) {
        localStorage.removeItem('selectedTripId');
      }

      this.noTripSelected = true;
      this.isLoading = false;
      this.showForm = false;

      this.cdr.detectChanges();
      return;
    }

    this.noTripSelected = false;
    this.cdr.detectChanges();
    this.initForm();
    this.loadData();
  }

  goToTripList(): void {
    this.router.navigate(['/tripList']);
  }

  get totalGastado(): number {
    return this.expenses.reduce((sum, expense) => sum + Number(expense.totalAmount), 0);
  }

  get gastoPropio(): number {
    if (!this.currentUserId) {
      return 0;
    }

    return this.expenses.reduce((sum, expense) => {
      const participant = expense.rawData?.participants?.find(
        (p: any) => Number(p.userId) === this.currentUserId
      );

      if (participant) {
        return sum + Number(participant.amountOwned || 0);
      }
      return sum;
    }, 0);
  }

  get deudasPendientes(): number {
    if (!this.currentUserId) {
      return 0;
    }

    return this.expenses.reduce((sum, expense) => {
      const participant = expense.rawData?.participants?.find(
        (p: any) => Number(p.userId) === this.currentUserId
      );

      if (participant) {
        const deuda = Number(participant.amountOwned || 0) - Number(participant.amountPaid || 0);
        return sum + (deuda > 0 ? deuda : 0);
      }
      return sum;
    }, 0);
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  applyFilters(): void {

  this.filterMinAmount = this.toNonNegativeOrNull(this.filterMinAmount);
  this.filterMaxAmount = this.toNonNegativeOrNull(this.filterMaxAmount);
  this.validateFilterAmountRange();

    let filtered = [...this.allExpenses];

    if (this.filterAmountRangeInvalid) return;

    if (this.filterActivityId !== null) {
      filtered = filtered.filter(expense => expense.activityId === this.filterActivityId);
    }

    if (this.filterMinAmount !== null && this.filterMinAmount > 0) {
      filtered = filtered.filter(expense => expense.totalAmount >= this.filterMinAmount!);
    }

    if (this.filterMaxAmount !== null && this.filterMaxAmount > 0) {
      filtered = filtered.filter(expense => expense.totalAmount <= this.filterMaxAmount!);
    }

    this.filteredExpenses = filtered;
    this.expenses = filtered;
  }

  clearFilters(): void {
    this.filterActivityId = null;
    this.filterMinAmount = null;
    this.filterMaxAmount = null;
    this.filterAmountRangeInvalid = false;
    this.filteredExpenses = [...this.allExpenses];
    this.expenses = [...this.allExpenses];
  }

  hasActiveFilters(): boolean {
    return this.filterActivityId !== null ||
           (this.filterMinAmount !== null && this.filterMinAmount > 0) ||
           (this.filterMaxAmount !== null && this.filterMaxAmount > 0);
  }

  private hasValidTrip(): boolean {
    if (!this.tripId || this.noTripSelected || !this.expenseForm) {
      return false;
    }
    return true;
  }

  private getCurrentUserId(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUserId = user.id ? Number(user.id) : null;
      },
      error: (err) => {
        console.error('❌ Error al obtener usuario actual:', err);
      }
    });
  }

  private loadData(): void {
    this.isLoading = true;

    this.tripService.getTripById(this.tripId).subscribe({
      next: (_trip) => {

        this.getCurrentUserId();

        this.loadTripData();
      },
      error: (err) => {
        console.error('❌ Error al verificar el viaje:', err);

        if (err.status === 403) {

          localStorage.removeItem('selectedTripId');

          this.noTripSelected = true;
          this.isLoading = false;
          this.showForm = false;
          this.tripId = '';
          this.cdr.detectChanges();

          alert('No tienes acceso a este viaje o tu sesión ha expirado. Por favor, inicia sesión nuevamente si el problema persiste.');
        } else if (err.status === 404) {
          console.warn('⚠️ Error 404: El viaje no existe');
          localStorage.removeItem('selectedTripId');

          this.noTripSelected = true;
          this.isLoading = false;
          this.showForm = false;
          this.tripId = '';
          this.cdr.detectChanges();

        } else {
          console.error('Error inesperado al cargar el viaje');
          this.isLoading = false;
        }
      }
    });
  }

  private loadTripData(): void {
    this.isLoading = true;

    forkJoin({
      users: this.tripService.getUsers(this.tripId),
      activities: this.activityService.listByTrip(Number(this.tripId)),
      expenses: this.expenseService.getExpensesByTrip(Number(this.tripId))
    }).subscribe({
      next: (result) => {
        this.tripMembers = result.users;
        this.activities = result.activities;


        this.allExpenses = result.expenses.map(exp => this.mapExpenseToDisplay(exp));
        this.expenses = [...this.allExpenses];
        this.filteredExpenses = [...this.allExpenses];

        this.isLoading = false;
      },
      error: (err) => {
        console.error('❌ Error al cargar datos del viaje:', err);
        this.isLoading = false;
      }
    });
  }

  private mapExpenseToDisplay(exp: ExpenseResponseDTO): any {

    return {
      id: exp.id,
      name: exp.name,
      note: exp.note || '',
      activityId: exp.activityId || null,
      activityName: exp.activityId
        ? this.activities.find(a => a.id === exp.activityId)?.name || 'Sin asignar'
        : 'Sin asignar',
      participants: exp.participants.map(p => {
        const user = this.tripMembers.find(m => Number(m.id) === p.userId);


        return {
          id: p.userId,
          photoUrl: user?.photoUrl || '/default-user-img.jpg'
        };
      }),
      totalAmount: exp.totalAmount,
      rawData: exp
    };
  }


  // Helpers para modo edición de consumidores
  private getSplitKey(index: number): number {
    const control = this.splits.at(index);
    return Number(control.get('userId')?.value ?? index);
  }

  isSplitEditing(index: number): boolean {
    return this.editingSplits.has(this.getSplitKey(index));
  }

  startSplitEdit(index: number): void {
    this.editingSplits.add(this.getSplitKey(index));
    // Usar setTimeout para asegurar que el input esté renderizado antes de seleccionar
    setTimeout(() => {
      const inputs = document.querySelectorAll('.participants-list .amount-input');
      // Encontrar el input correcto basado en el índice
      let splitInputIndex = 0;
      for (let i = 0; i <= index && splitInputIndex < inputs.length; i++) {
        if (this.isSplitEditing(i)) {
          if (i === index) {
            const input = inputs[splitInputIndex] as HTMLInputElement;
            if (input) {
              input.focus();
              input.select();
            }
            break;
          }
          splitInputIndex++;
        }
      }
    }, 0);
  }

  finishSplitEdit(index: number): void {
    this.editingSplits.delete(this.getSplitKey(index));
  }

  // Helpers para modo edición de pagadores
  private getPayerKey(index: number): number {
    const control = this.payers.at(index);
    return Number(control.get('userId')?.value ?? index);
  }

  isPayerEditing(index: number): boolean {
    return this.editingPayers.has(this.getPayerKey(index));
  }

  startPayerEdit(index: number): void {
    this.editingPayers.add(this.getPayerKey(index));
    // Usar setTimeout para asegurar que el input esté renderizado antes de seleccionar
    setTimeout(() => {
      const inputs = document.querySelectorAll('.participant-row .amount-input');
      const input = inputs[index] as HTMLInputElement;
      if (input) {
        input.focus();
        input.select();
      }
    }, 0);
  }

  finishPayerEdit(index: number): void {
    const control = this.payers.at(index);
    const amountControl = control.get('amountPaid');
    const currentValue = amountControl?.value;

    // Si está vacío o null, establecer en 0
    if (currentValue === null || currentValue === undefined || currentValue === '') {
      amountControl?.setValue(0);
    }

    // Marcar como tocado para mostrar validación
    amountControl?.markAsTouched();

    this.editingPayers.delete(this.getPayerKey(index));
  }

  // Helpers para modo edición de monto total
  startTotalAmountEdit(): void {
    this.editingTotalAmount = true;
    // Usar setTimeout para asegurar que el input esté renderizado antes de seleccionar
    setTimeout(() => {
      const input = document.querySelector('.total-amount-input') as HTMLInputElement;
      if (input) {
        input.focus();
        input.select();
      }
    }, 0);
  }

  finishTotalAmountEdit(): void {
    const currentValue = this.totalAmountControl?.value;

    // Si está vacío o null, establecer en 0
    if (currentValue === null || currentValue === undefined || currentValue === '') {
      this.totalAmountControl?.setValue(0);
    }

    // Marcar como tocado para mostrar validación
    this.totalAmountControl?.markAsTouched();

    this.editingTotalAmount = false;
  }

  private initForm(): void {
    this.expenseForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100), this.notOnlyWhitespaceValidator()]],
      note: ['', [Validators.maxLength(255)]],
      // Máximo permitido $5.000.000
      totalAmount: [0, [Validators.required, Validators.min(0.01), this.maxTotalValidator()]],
      activityId: [null],
      payers: this.fb.array([], [Validators.required, this.atLeastOneValidator()]),
      splits: this.fb.array([], [Validators.required, this.atLeastOneValidator()])
    }, { validators: [this.totalPayersValidator(), this.totalSplitsValidator()] });

    // Suscribir cambios en el control 'name' para forzar el error 'whitespace' y marcar touched/dirty
    const nameCtrl = this.expenseForm.get('name');
    if (nameCtrl) {
      nameCtrl.valueChanges.subscribe((val: any) => {
        if (val === null || val === undefined) {
          if (nameCtrl.hasError('whitespace')) {
            const errors = { ...(nameCtrl.errors || {}) } as any;
            delete errors['whitespace'];
            const keys = Object.keys(errors);
            nameCtrl.setErrors(keys.length > 0 ? errors : null);
          }
          return;
        }

        if (typeof val === 'string') {
          const isOnlySpaces = val.length > 0 && val.trim().length === 0;
          if (isOnlySpaces) {
            const existing = nameCtrl.errors || {};
            if (!existing['whitespace']) {
              nameCtrl.setErrors({ ...existing, whitespace: true });
            }
            // Asegurar que el mensaje se muestre incluso mientras escribe
            nameCtrl.markAsDirty();
            nameCtrl.markAsTouched();
          } else {
            if (nameCtrl.hasError('whitespace')) {
              const errors = { ...(nameCtrl.errors || {}) } as any;
              delete errors['whitespace'];
              const keys = Object.keys(errors);
              nameCtrl.setErrors(keys.length > 0 ? errors : null);
            }
          }
        }
      });
    }
  }

  // Validador personalizado para el máximo total (redondea a 2 decimales antes de comparar)
  private maxTotalValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const raw = control.value;
      if (raw === null || raw === undefined || raw === '') return null;
      const num = Number(raw);
      if (isNaN(num)) return null;
      const rounded = Math.round(num * 100) / 100;
      if (rounded > this.MAX_TOTAL_AMOUNT) {
        return { max: { max: this.MAX_TOTAL_AMOUNT, actual: rounded } } as any;
      }
      return null;
    };
  }

  get payers(): FormArray {
    if (!this.expenseForm) return this.fb.array([]);
    return this.expenseForm.get('payers') as FormArray;
  }

  get splits(): FormArray {
    if (!this.expenseForm) return this.fb.array([]);
    return this.expenseForm.get('splits') as FormArray;
  }

  get nameControl() {
    if (!this.expenseForm) return null;
    return this.expenseForm.get('name');
  }

  get totalAmountControl() {
    if (!this.expenseForm) return null;
    return this.expenseForm.get('totalAmount');
  }

  addPayer(user: TravelerResponse): void {
    if (!this.expenseForm || this.noTripSelected) {
      console.warn('No se puede agregar pagador: formulario no inicializado o sin viaje seleccionado');
      return;
    }

    console.log('addPayer llamado con usuario:', user);

    const exists = this.payers.controls.some(
      control => String(control.get('userId')?.value) === String(user.id)
    );

    if (exists) {
      console.log('Usuario ya existe en payers');
      return;
    }

    const payerGroup = this.fb.group({
      userId: [user.id, Validators.required],
      userName: [user.name],
      amountPaid: [0, [Validators.required, Validators.min(0.01)]]
    });

    this.payers.push(payerGroup);
    this.editingPayers.add(Number(user.id));
    console.log('Payer agregado. Total payers:', this.payers.length);
  }

  // Manejar selección de pagador desde dropdown
  onSelectPayer(userId: string): void {
    console.log('onSelectPayer llamado con userId:', userId);

    if (!userId) {
      console.log('userId está vacío');
      return;
    }

    const user = this.tripMembers.find(m => String(m.id) === String(userId));
    console.log('Usuario encontrado:', user);

    if (user) {
      this.addPayer(user);
    } else {
      console.log('No se encontró el usuario con id:', userId);
    }
  }

  // Remover pagador
  removePayer(index: number): void {
    const key = this.getPayerKey(index);
    this.payers.removeAt(index);
    this.editingPayers.delete(key);
  }

  // Agregar consumidor
  addSplit(user: TravelerResponse): void {
    if (!this.expenseForm || this.noTripSelected) {
      console.warn('No se puede agregar consumidor: formulario no inicializado o sin viaje seleccionado');
      return;
    }

    console.log('addSplit llamado con usuario:', user);

    const exists = this.splits.controls.some(
      control => String(control.get('userId')?.value) === String(user.id)
    );

    if (exists) {
      console.log('Usuario ya existe en splits');
      return;
    }

    const splitGroup = this.fb.group({
      userId: [user.id, Validators.required],
      userName: [user.name],
      amountOwed: [0, [Validators.required, Validators.min(0)]]
    });

    this.splits.push(splitGroup);
    this.editingSplits.add(Number(user.id));
    console.log('Split agregado. Total splits:', this.splits.length);

    if (this.divisionType === 'equal') {
      this.distributeEqually();
    }
  }

  onSelectSplit(userId: string): void {
    console.log('onSelectSplit llamado con userId:', userId);
    console.log('tripMembers:', this.tripMembers);

    if (!userId) {
      console.log('userId está vacío');
      return;
    }

    const user = this.tripMembers.find(m => String(m.id) === String(userId));
    console.log('Usuario encontrado:', user);

    if (user) {
      this.addSplit(user);
    } else {
      console.log('No se encontró el usuario con id:', userId);
    }
  }

  removeSplit(index: number): void {
    const key = this.getSplitKey(index);
    this.splits.removeAt(index);
    this.editingSplits.delete(key);

    if (this.divisionType === 'equal') {
      this.distributeEqually();
    }
  }

  changeDivisionType(type: 'equal' | 'custom'): void {
    this.divisionType = type;

    if (type === 'equal') {
      this.distributeEqually();
      this.editingSplits.clear();
    }
  }

  toggleDivisionType(): void {
    const newType = this.divisionType === 'equal' ? 'custom' : 'equal';
    this.changeDivisionType(newType);
  }

  private distributeEqually(): void {
    const total = this.expenseForm.get('totalAmount')?.value || 0;
    const count = this.splits.length;

    if (count === 0) return;

    const amountPerPerson = total / count;

    this.splits.controls.forEach(control => {
      control.get('amountOwed')?.setValue(amountPerPerson, { emitEvent: false });
    });
  }

  private atLeastOneValidator() {
    return (formArray: AbstractControl): ValidationErrors | null => {
      const arr = formArray as FormArray;
      return arr.length > 0 ? null : { atLeastOne: true };
    };
  }

  private totalPayersValidator() {
    return (form: AbstractControl): ValidationErrors | null => {
      const formGroup = form as FormGroup;
      const total = formGroup.get('totalAmount')?.value || 0;
      const payersArray = formGroup.get('payers') as FormArray;

      if (!payersArray || payersArray.length === 0) return null;

      const sumPaid = payersArray.controls.reduce((sum, control) => {
        return sum + (control.get('amountPaid')?.value || 0);
      }, 0);

      return Math.abs(sumPaid - total) < 0.01 ? null : { payersTotal: true };
    };
  }

  private totalSplitsValidator() {
    return (form: AbstractControl): ValidationErrors | null => {
      const formGroup = form as FormGroup;
      const total = formGroup.get('totalAmount')?.value || 0;
      const splitsArray = formGroup.get('splits') as FormArray;

      if (!splitsArray || splitsArray.length === 0) return null;

      const sumOwed = splitsArray.controls.reduce((sum, control) => {
        return sum + (control.get('amountOwed')?.value || 0);
      }, 0);

      return Math.abs(sumOwed - total) < 0.01 ? null : { splitsTotal: true };
    };
  }

  getAvailablePayers(): TravelerResponse[] {
    return this.tripMembers.filter(member =>
      !this.payers.controls.some(control => String(control.get('userId')?.value) === String(member.id))
    );
  }

  getAvailableSplits(): TravelerResponse[] {
    return this.tripMembers.filter(member =>
      !this.splits.controls.some(control => String(control.get('userId')?.value) === String(member.id))
    );
  }

  getUserPhotoUrl(userId: number | null | undefined): string {
    if (!userId) {
      return '/default-user-img.jpg';
    }
    const user = this.tripMembers.find(m => Number(m.id) === Number(userId));
    return user?.photoUrl || '/default-user-img.jpg';
  }

  startFilterMinAmountEdit(): void {
    this.editingFilterMinAmount = true;
    setTimeout(() => {
      const input = document.querySelector('.filter-min-amount-input') as HTMLInputElement;
      if (input) {
        input.focus();
        input.select();
      }
    }, 0);
  }

  finishFilterMinAmountEdit(): void {
    this.filterMinAmount = this.toNonNegativeOrNull(this.filterMinAmount);
      this.validateFilterAmountRange();
      this.editingFilterMinAmount = false;
  }

  startFilterMaxAmountEdit(): void {
    this.editingFilterMaxAmount = true;
    setTimeout(() => {
      const input = document.querySelector('.filter-max-amount-input') as HTMLInputElement;
      if (input) {
        input.focus();
        input.select();
      }
    }, 0);
  }

  finishFilterMaxAmountEdit(): void {
   this.filterMaxAmount = this.toNonNegativeOrNull(this.filterMaxAmount);
     this.validateFilterAmountRange();
     this.editingFilterMaxAmount = false;
  }



  onTotalAmountChange(): void {

    if (this.divisionType === 'equal') {
      this.distributeEqually();
    }

    const control = this.totalAmountControl;
    if (!control) return;

    let value = control.value;

    if (value === null || value === undefined || value === '') return;

    // Normalizar y redondear a 2 decimales para evitar problemas de precisión
    const numericRaw = Number(String(value).replace(/,/g, ''));
    const numericValue = isNaN(numericRaw) ? NaN : Math.round(numericRaw * 100) / 100;

    if (isNaN(numericValue)) {
      control.setValue(null, { emitEvent: false });
      return;
    }

    if (numericValue > this.MAX_TOTAL_AMOUNT) {
      // Forzar el valor al máximo redondeado a 2 decimales
      const roundedMax = Math.round(this.MAX_TOTAL_AMOUNT * 100) / 100;
      control.setValue(roundedMax, { emitEvent: false });
    }
    else if (numericValue < 0) {
      control.setValue(0, { emitEvent: false });
    }
  }

  onSubmit(): void {
    if (!this.hasValidTrip()) {
      alert('No hay un viaje seleccionado. Por favor selecciona un viaje primero.');
      this.router.navigate(['/tripList']);
      return;
    }

    if (this.expenseForm.invalid) {
      this.expenseForm.markAllAsTouched();
      return;
    }

    const formValue = this.expenseForm.value;

    if (this.selectedExpenseId) {
      const splitsToSend = formValue.splits.map((s: any) => ({
        userId: Number(s.userId),
        amountOwed: Number(s.amountOwed)
      }));

      const updateDTO: ExpenseUpdateDTO = {
        expenseId: this.selectedExpenseId,
        tripId: Number(this.tripId),
        activityId: formValue.activityId || null,
        name: formValue.name,
        note: formValue.note || '',
        totalAmount: formValue.totalAmount,
        payers: formValue.payers.map((p: any) => ({
          userId: Number(p.userId),
          amountPaid: Number(p.amountPaid)
        })),
        splits: splitsToSend,
        customSplit: this.divisionType === 'custom'
      };

      console.log('📤 DTO a actualizar:', JSON.stringify(updateDTO, null, 2));

      this.expenseService.updateExpense(updateDTO).subscribe({
        next: (response) => {
          console.log('Gasto actualizado:', response);
          const updatedExpense = this.mapExpenseToDisplay(response);

          const allIndex = this.allExpenses.findIndex(e => e.id === this.selectedExpenseId);
          if (allIndex !== -1) {
            this.allExpenses[allIndex] = updatedExpense;
          }

          if (this.hasActiveFilters()) {
            this.applyFilters();
          } else {
            this.expenses = [...this.allExpenses];
          }

          this.onCancel();
        },
        error: (err) => {
          console.error('❌ Error al actualizar gasto:', err);
          console.error('❌ Error completo:', JSON.stringify(err, null, 2));

          let errorMsg = 'Error al actualizar el gasto. ';

          if (err.error?.message) {
            errorMsg += err.error.message;
          } else if (err.message) {
            errorMsg += err.message;
          } else {
            errorMsg += 'Por favor intenta de nuevo.';
          }

          alert(errorMsg);
        }
      });
    } else {
      const splitsToSend = formValue.splits.map((s: any) => ({
        userId: Number(s.userId),
        amountOwed: Number(s.amountOwed)
      }));

      const createDTO: CreateExpenseDTO = {
        tripId: Number(this.tripId),
        activityId: formValue.activityId || null,
        name: formValue.name,
        note: formValue.note || '',
        totalAmount: formValue.totalAmount,
        payers: formValue.payers.map((p: any) => ({
          userId: Number(p.userId),
          amountPaid: Number(p.amountPaid)
        })),
        splits: splitsToSend,
        customSplit: this.divisionType === 'custom'
      };

      console.log('📤 DTO a crear:', JSON.stringify(createDTO, null, 2));

      const invalidPayers = createDTO.payers.filter(p =>
        !this.tripMembers.some(m => Number(m.id) === p.userId)
      );

      if (invalidPayers.length > 0) {
        console.error('❌ ERROR: Pagadores inválidos detectados:', invalidPayers);
        return;
      }

      this.expenseService.createExpense(createDTO).subscribe({
        next: (response) => {
          console.log('Gasto creado:', response);
          const newExpense = this.mapExpenseToDisplay(response);
          this.allExpenses.push(newExpense);

          if (this.hasActiveFilters()) {
            this.applyFilters();
          } else {
            this.expenses = [...this.allExpenses];
          }

          this.onCancel();
        },
        error: (err) => {
          console.error('❌ Error al crear gasto:', err);
          console.error('❌ Error completo:', JSON.stringify(err, null, 2));

          let errorMsg = 'Error al crear el gasto. ';

          if (err.error?.message) {
            errorMsg += err.error.message;
          } else if (err.message) {
            errorMsg += err.message;
          } else {
            errorMsg += 'Por favor verifica:\n';
            errorMsg += '- Que hayas agregado al menos un PAGADOR\n';
            errorMsg += '- Que la suma de pagadores sea igual al monto total\n';
            errorMsg += '- Que todos los campos requeridos estén completos';
          }

          alert(errorMsg);
        }
      });
    }
  }

  onCancel(): void {
    this.expenseForm.reset();
    this.payers.clear();
    this.splits.clear();
    this.divisionType = 'equal';
    this.editingSplits.clear();
    this.editingPayers.clear();
    this.editingTotalAmount = false;
    this.showForm = false;
    this.selectedExpenseId = null;
    this.selectedExpense = null;
  }

  deleteExpense(expenseId: number, event: Event): void {
    event.stopPropagation();
    this.expenseToDelete = expenseId;
    this.showDeleteModal = true;
  }

  cancelDelete(): void {
    this.showDeleteModal = false;
    this.expenseToDelete = null;
  }

  confirmDelete(): void {
    if (!this.expenseToDelete) {
      return;
    }

    const expenseId = this.expenseToDelete;
    console.log('🗑️ Eliminando gasto con ID:', expenseId);

    this.showDeleteModal = false;
    this.expenseToDelete = null;

    this.expenseService.deleteExpense(expenseId).subscribe({
      next: () => {
        this.allExpenses = this.allExpenses.filter(e => e.id !== expenseId);

        if (this.hasActiveFilters()) {
          this.applyFilters();
        } else {
          this.expenses = [...this.allExpenses];
        }

        if (this.selectedExpenseId === expenseId) {
          this.onCancel();
        }
      },
      error: (err) => {
        console.error('❌ Error al eliminar gasto:', err);

        let errorMsg = 'Error al eliminar el gasto. ';
        if (err.error?.message) {
          errorMsg += err.error.message;
        } else if (err.status === 403) {
          errorMsg += 'No tenés permisos para eliminar este gasto.';
        } else if (err.status === 404) {
          errorMsg += 'El gasto ya no existe.';
        } else {
          errorMsg += 'Por favor intentá de nuevo.';
        }

        alert(errorMsg);
      }
    });
  }

private toNonNegativeOrNull(value: any): number | null {
  if (value === null || value === undefined || value === '') return null;

  const n = Number(value);
  if (Number.isNaN(n)) return null;

  return n < 0 ? 0 : n;
}

onFilterMinAmountChange(value: any): void {
  this.filterMinAmount = this.toNonNegativeOrNull(value);
  this.validateFilterAmountRange();
}

onFilterMaxAmountChange(value: any): void {
  this.filterMaxAmount = this.toNonNegativeOrNull(value);
  this.validateFilterAmountRange();
}

blockNegativeKey(event: KeyboardEvent): void {
  const blocked = ['-', 'e', 'E'];
  if (blocked.includes(event.key)) {
    event.preventDefault();
  }
}

onPasteNonNegative(event: ClipboardEvent, target: 'min' | 'max'): void {
  const text = event.clipboardData?.getData('text') ?? '';
  const n = this.toNonNegativeOrNull(text);

  if (n === null && text.trim() !== '') {
    event.preventDefault();
    return;
  }

  if (n !== null && n >= 0) {
    return;
  }

  event.preventDefault();
  if (target === 'min') this.filterMinAmount = 0;
  else this.filterMaxAmount = 0;
}

private validateFilterAmountRange(): void {
  if (this.filterMinAmount === null || this.filterMaxAmount === null) {
    this.filterAmountRangeInvalid = false;
    return;
  }
  this.filterAmountRangeInvalid = this.filterMaxAmount < this.filterMinAmount;
}

  showCreateForm(): void {
    if (!this.expenseForm || this.noTripSelected) {
      console.warn('No se puede crear gasto: sin viaje seleccionado');
      alert('Por favor selecciona un viaje primero');
      return;
    }

    if (this.showForm) {
      this.onCancel();
      return;
    }

    this.showForm = true;
    this.selectedExpenseId = null;
    this.selectedExpense = null;
    this.expenseForm.reset();
    this.payers.clear();
    this.splits.clear();
    this.divisionType = 'equal';
    this.editingSplits.clear();
    this.editingPayers.clear();
    this.editingTotalAmount = true;
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
        // Devolver la clave 'whitespace' para que coincida con la plantilla
        return { whitespace: true };
      }

      return null;
    };
  }

  viewExpenseDetails(expense: any): void {
    if (!this.hasValidTrip()) {
      console.warn('No se puede ver detalles: sin viaje seleccionado');
      return;
    }

    this.selectedExpense = expense;
    this.selectedExpenseId = expense.id;
    this.showForm = true;

    this.expenseService.getExpenseById(expense.id).subscribe({
      next: (fullExpense) => {

        this.expenseForm.patchValue({
          name: fullExpense.name,
          note: fullExpense.note || '',
          totalAmount: fullExpense.totalAmount,
          activityId: fullExpense.activityId || null
        });

        this.payers.clear();
        this.splits.clear();
        this.editingPayers.clear();
        this.editingSplits.clear();

        if (fullExpense.participants && fullExpense.participants.length > 0) {

          fullExpense.participants.forEach((participant: any) => {

            const user = this.tripMembers.find(m => Number(m.id) === participant.userId);


            if (participant.amountPaid && participant.amountPaid > 0) {
              const payerGroup = this.fb.group({
                userId: [participant.userId, Validators.required],
                userName: [user?.name],
                amountPaid: [participant.amountPaid, [Validators.required, Validators.min(0.01)]]
              });
              this.payers.push(payerGroup);
            }

            if (participant.amountOwned && participant.amountOwned > 0) {
              const splitGroup = this.fb.group({
                userId: [participant.userId, Validators.required],
                userName: [user?.name],
                amountOwed: [participant.amountOwned, [Validators.required, Validators.min(0)]]
              });
              this.splits.push(splitGroup);
            }
          });


          if (this.splits.length > 0) {
            this.divisionType = 'custom';
          } else {
            this.divisionType = 'equal';
          }

        }
      },
      error: (err) => {
        console.error('Error al cargar detalles del gasto:', err);
        alert('Error al cargar los detalles del gasto.');
        this.onCancel();
      }
    });
  }
}
