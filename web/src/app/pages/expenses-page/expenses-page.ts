import { Component, OnInit } from '@angular/core';
import { Test } from '../test/test';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray, AbstractControl, ValidationErrors } from '@angular/forms';
import { CreateExpenseDTO } from '../../models/ExpenseCreate';
import { TravelerResponse } from '../../models/TravelerResponse';
import { ActivityResponseDTO } from '../../models/ActivityResponse';

@Component({
  selector: 'app-expenses-page',
  imports: [Test, CommonModule, ReactiveFormsModule],
  templateUrl: './expenses-page.html',
  styleUrl: './expenses-page.scss',
})
export class ExpensesPage implements OnInit {
  expenseForm!: FormGroup;

  // Mock data - reemplazar con datos reales del servicio
  tripMembers: TravelerResponse[] = [];
  activities: ActivityResponseDTO[] = [];

  // Lista de gastos
  expenses: any[] = [];
  nextExpenseId: number = 1;

  // Control del tipo de división
  divisionType: 'equal' | 'custom' = 'equal';

  // Control del estado del sidebar
  showForm: boolean = false;
  selectedExpenseId: number | null = null;
  selectedExpense: any = null;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.initForm();
    this.loadMockData();
  }

  private initForm(): void {
    this.expenseForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      note: ['', [Validators.maxLength(255)]],
      totalAmount: [0, [Validators.required, Validators.min(0.01)]],
      activityId: [null], // Puede ser null si no se asigna
      payers: this.fb.array([], [Validators.required, this.atLeastOneValidator()]),
      splits: this.fb.array([], [Validators.required, this.atLeastOneValidator()])
    }, { validators: [this.totalPayersValidator(), this.totalSplitsValidator()] });
  }

  // Getters para FormArrays
  get payers(): FormArray {
    return this.expenseForm.get('payers') as FormArray;
  }

  get splits(): FormArray {
    return this.expenseForm.get('splits') as FormArray;
  }

  get nameControl() {
    return this.expenseForm.get('name');
  }

  get totalAmountControl() {
    return this.expenseForm.get('totalAmount');
  }

  // Agregar pagador
  addPayer(user: TravelerResponse): void {
    // Verificar que no esté ya agregado
    const exists = this.payers.controls.some(
      control => control.get('userId')?.value === user.id
    );

    if (exists) return;

    const payerGroup = this.fb.group({
      userId: [user.id, Validators.required],
      userName: [user.name],
      amountPaid: [0, [Validators.required, Validators.min(0)]]
    });

    this.payers.push(payerGroup);
  }

  // Manejar selección de pagador desde dropdown
  onSelectPayer(userId: string): void {
    if (!userId) return;

    const user = this.tripMembers.find(m => m.id === userId);
    if (user) {
      this.addPayer(user);
    }
  }

  // Remover pagador
  removePayer(index: number): void {
    this.payers.removeAt(index);
  }

  // Agregar consumidor
  addSplit(user: TravelerResponse): void {
    const exists = this.splits.controls.some(
      control => control.get('userId')?.value === user.id
    );

    if (exists) return;

    const splitGroup = this.fb.group({
      userId: [user.id, Validators.required],
      userName: [user.name],
      amountOwed: [0, [Validators.required, Validators.min(0)]]
    });

    this.splits.push(splitGroup);

    // Si es división igual, recalcular automáticamente
    if (this.divisionType === 'equal') {
      this.distributeEqually();
    }
  }

  // Manejar selección de consumidor desde dropdown
  onSelectSplit(userId: string): void {
    if (!userId) return;

    const user = this.tripMembers.find(m => m.id === userId);
    if (user) {
      this.addSplit(user);
    }
  }

  // Remover consumidor
  removeSplit(index: number): void {
    this.splits.removeAt(index);

    // Si es división igual, recalcular automáticamente
    if (this.divisionType === 'equal') {
      this.distributeEqually();
    }
  }

  // Cambiar tipo de división
  changeDivisionType(type: 'equal' | 'custom'): void {
    this.divisionType = type;

    if (type === 'equal') {
      this.distributeEqually();
    }
  }

  // Distribuir montos equitativamente
  private distributeEqually(): void {
    const total = this.expenseForm.get('totalAmount')?.value || 0;
    const count = this.splits.length;

    if (count === 0) return;

    const amountPerPerson = total / count;

    this.splits.controls.forEach(control => {
      control.get('amountOwed')?.setValue(amountPerPerson, { emitEvent: false });
    });
  }

  // Validador: al menos un elemento en el array
  private atLeastOneValidator() {
    return (formArray: AbstractControl): ValidationErrors | null => {
      const arr = formArray as FormArray;
      return arr.length > 0 ? null : { atLeastOne: true };
    };
  }

  // Validador: suma de pagadores = total
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

  // Validador: suma de consumidores = total
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

  // Obtener usuarios disponibles para agregar como pagadores
  getAvailablePayers(): TravelerResponse[] {
    return this.tripMembers.filter(member =>
      !this.payers.controls.some(control => control.get('userId')?.value === member.id)
    );
  }

  // Obtener usuarios disponibles para agregar como consumidores
  getAvailableSplits(): TravelerResponse[] {
    return this.tripMembers.filter(member =>
      !this.splits.controls.some(control => control.get('userId')?.value === member.id)
    );
  }

  // Cuando cambia el monto total y es división igual
  onTotalAmountChange(): void {
    if (this.divisionType === 'equal') {
      this.distributeEqually();
    }
  }

  // Guardar gasto (crear o editar)
  onSubmit(): void {
    if (this.expenseForm.invalid) {
      this.expenseForm.markAllAsTouched();
      return;
    }

    const formValue = this.expenseForm.value;

    const expenseDTO: CreateExpenseDTO = {
      name: formValue.name,
      note: formValue.note || '',
      totalAmount: formValue.totalAmount,
      activityId: formValue.activityId,
      payers: formValue.payers.map((p: any) => ({
        userId: p.userId,
        amountPaid: p.amountPaid
      })),
      splits: formValue.splits.map((s: any) => ({
        userId: s.userId,
        amountOwed: s.amountOwed
      })),
      isCustomSplit: this.divisionType === 'custom'
    };

    console.log('Expense DTO:', expenseDTO);

    // Crear objeto de gasto para mostrar en la tabla
    const expenseData = {
      id: this.selectedExpenseId || this.nextExpenseId++,
      name: formValue.name,
      note: formValue.note || '',
      activityId: formValue.activityId,
      activityName: formValue.activityId
        ? this.activities.find(a => a.id === formValue.activityId)?.name
        : 'Sin asignar',
      participants: formValue.payers.map((p: any) => {
        const user = this.tripMembers.find(m => m.id === p.userId);
        return {
          id: p.userId,
          photoUrl: user?.photoUrl || `https://i.pravatar.cc/40?img=${p.userId}`
        };
      }),
      totalAmount: formValue.totalAmount,
      payers: formValue.payers,
      splits: formValue.splits,
      isCustomSplit: this.divisionType === 'custom'
    };

    if (this.selectedExpenseId) {
      // MODO EDICIÓN: Actualizar gasto existente
      const index = this.expenses.findIndex(e => e.id === this.selectedExpenseId);
      if (index !== -1) {
        this.expenses[index] = expenseData;
        console.log('Gasto actualizado:', expenseData);
      }
    } else {
      // MODO CREACIÓN: Agregar nuevo gasto
      this.expenses.push(expenseData);
      console.log('Gasto creado:', expenseData);
    }

    // Limpiar y cerrar formulario
    this.onCancel();

    // Aquí llamarías al servicio para enviar al backend
    // if (this.selectedExpenseId) {
    //   this.expenseService.updateExpense(this.selectedExpenseId, expenseDTO).subscribe(...)
    // } else {
    //   this.expenseService.createExpense(expenseDTO).subscribe(...)
    // }
  }

  // Cancelar
  onCancel(): void {
    this.expenseForm.reset();
    this.payers.clear();
    this.splits.clear();
    this.divisionType = 'equal';
    this.showForm = false;
    this.selectedExpenseId = null;
    this.selectedExpense = null;
  }

  // Mostrar formulario para crear nuevo gasto
  showCreateForm(): void {
    this.showForm = true;
    this.selectedExpenseId = null;
    this.selectedExpense = null;
    this.expenseForm.reset();
    this.payers.clear();
    this.splits.clear();
    this.divisionType = 'equal';
  }

  // Ver detalles de un gasto seleccionado (abrir formulario con datos prellenados)
  viewExpenseDetails(expense: any): void {
    this.selectedExpense = expense;
    this.selectedExpenseId = expense.id;
    this.showForm = true;

    // Prellenar el formulario con los datos del gasto
    this.expenseForm.patchValue({
      name: expense.name,
      note: expense.note || '',
      totalAmount: expense.totalAmount,
      activityId: expense.activityId || null
    });

    // Limpiar los arrays antes de rellenarlos
    this.payers.clear();
    this.splits.clear();

    // Rellenar pagadores (si existen en el gasto)
    if (expense.payers && expense.payers.length > 0) {
      expense.payers.forEach((payer: any) => {
        const user = this.tripMembers.find(m => m.id === payer.userId);
        if (user) {
          const payerGroup = this.fb.group({
            userId: [payer.userId, Validators.required],
            userName: [user.name],
            amountPaid: [payer.amountPaid, [Validators.required, Validators.min(0)]]
          });
          this.payers.push(payerGroup);
        }
      });
    }

    // Rellenar consumidores (si existen en el gasto)
    if (expense.splits && expense.splits.length > 0) {
      expense.splits.forEach((split: any) => {
        const user = this.tripMembers.find(m => m.id === split.userId);
        if (user) {
          const splitGroup = this.fb.group({
            userId: [split.userId, Validators.required],
            userName: [user.name],
            amountOwed: [split.amountOwed, [Validators.required, Validators.min(0)]]
          });
          this.splits.push(splitGroup);
        }
      });
    }

    // Si no hay datos guardados de payers/splits, usar los participantes
    if ((!expense.payers || expense.payers.length === 0) && expense.participants) {
      expense.participants.forEach((participant: any) => {
        const user = this.tripMembers.find(m => m.id === participant.id);
        if (user) {
          const payerGroup = this.fb.group({
            userId: [participant.id, Validators.required],
            userName: [user.name],
            amountPaid: [0, [Validators.required, Validators.min(0)]]
          });
          this.payers.push(payerGroup);
        }
      });
    }

    // Establecer el tipo de división
    this.divisionType = expense.isCustomSplit ? 'custom' : 'equal';
  }


  // Mock data para testing
  private loadMockData(): void {
    this.tripMembers = [
      new TravelerResponse('1', 'Juan Pérez', 'juan@example.com', '123456', 'https://i.pravatar.cc/40?img=1', 'Juancho', 'About Juan', '1990-01-01', 34),
      new TravelerResponse('2', 'María García', 'maria@example.com', '123457', 'https://i.pravatar.cc/40?img=2', 'Mary', 'About Maria', '1992-05-15', 32),
      new TravelerResponse('3', 'Pedro López', 'pedro@example.com', '123458', 'https://i.pravatar.cc/40?img=3', 'Pedrito', 'About Pedro', '1988-08-20', 36),
    ];

    this.activities = [
      { id: 1, name: 'Cena en restaurante', date: '2026-02-10', description: 'Cena grupal', cost: 0, startTime: '20:00', endTime: '22:00', tripId: 1 },
      { id: 2, name: 'Excursión', date: '2026-02-11', description: 'Tour por la ciudad', cost: 0, startTime: '09:00', endTime: '18:00', tripId: 1 },
    ];

    // Agregar algunos gastos de ejemplo
    this.expenses = [
      {
        id: this.nextExpenseId++,
        name: 'Cena italiana',
        note: 'Pizza y pasta',
        activityId: 1,
        activityName: 'Cena en restaurante',
        participants: [
          { id: '1', photoUrl: 'https://i.pravatar.cc/40?img=1' },
          { id: '2', photoUrl: 'https://i.pravatar.cc/40?img=2' },
          { id: '3', photoUrl: 'https://i.pravatar.cc/40?img=3' }
        ],
        totalAmount: 4500,
        payers: [
          { userId: '1', amountPaid: 4500 }
        ],
        splits: [
          { userId: '1', amountOwed: 1500 },
          { userId: '2', amountOwed: 1500 },
          { userId: '3', amountOwed: 1500 }
        ],
        isCustomSplit: false
      },
      {
        id: this.nextExpenseId++,
        name: 'Entradas museo',
        note: 'Tour guiado incluido',
        activityId: 2,
        activityName: 'Excursión',
        participants: [
          { id: '1', photoUrl: 'https://i.pravatar.cc/40?img=1' },
          { id: '2', photoUrl: 'https://i.pravatar.cc/40?img=2' }
        ],
        totalAmount: 2000,
        payers: [
          { userId: '2', amountPaid: 2000 }
        ],
        splits: [
          { userId: '1', amountOwed: 1000 },
          { userId: '2', amountOwed: 1000 }
        ],
        isCustomSplit: false
      },
      {
        id: this.nextExpenseId++,
        name: 'Transporte',
        note: 'Uber compartido',
        activityId: null,
        activityName: 'Sin asignar',
        participants: [
          { id: '1', photoUrl: 'https://i.pravatar.cc/40?img=1' },
          { id: '2', photoUrl: 'https://i.pravatar.cc/40?img=2' },
          { id: '3', photoUrl: 'https://i.pravatar.cc/40?img=3' }
        ],
        totalAmount: 1200,
        payers: [
          { userId: '3', amountPaid: 1200 }
        ],
        splits: [
          { userId: '1', amountOwed: 400 },
          { userId: '2', amountOwed: 400 },
          { userId: '3', amountOwed: 400 }
        ],
        isCustomSplit: false
      }
    ];
  }
}
