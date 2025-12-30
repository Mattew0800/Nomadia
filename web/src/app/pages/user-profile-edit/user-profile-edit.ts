import { Component } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../../services/auth-service';
import { UpdateUserResponse } from '../../models/UpdateUserResponse';
import { UserService } from '../../services/user-service';
import { putResponse } from '../../models/putResponse';
import { ErrorResponse } from '../../models/ErrorResponse';
import { User } from '../../models/User';
import { Test } from '../test/test';
import {AuthErrorResponse} from '../../models/AuthErrorResponse';

@Component({
  selector: 'app-user-profile-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgClass, Test],
  templateUrl: './user-profile-edit.html',
  styleUrl: './user-profile-edit.scss'
})
export class UserProfileEdit {

  form: FormGroup;
  submitted = false;
  photoFile?: File;
  photoPreview?: string;
  msgOk?: string;
  msgError?: string;
  loading = true;
  errorMessages: string = "";
  user?: User;
  DEFAULT_PHOTO = 'default-user-img.jpg';




  constructor(private http: HttpClient, private router: Router, public authService: AuthService, public userService: UserService) {

    this.form = new FormGroup({
      name: new FormControl('', [
        Validators.required,
        Validators.pattern(/^(?:[A-Za-zÁÉÍÓÚáéíóúÑñ]{2,}|de|del|la|los|san)(?:\s(?:[A-Za-zÁÉÍÓÚáéíóúÑñ]{2,}|de|del|la|los|san))+$/i)
      ]),
      nick: new FormControl('', Validators.maxLength(10)),
      email: new FormControl({ value: '', disabled: false }, [
        Validators.required,
        Validators.email
      ]),
      phone: new FormControl('', [
        Validators.minLength(10),
        Validators.maxLength(13),
        Validators.pattern(/^[0-9]+$/)
      ]),
      birth: new FormControl('',[Validators.pattern(/^\d{4}-\d{2}-\d{2}$/), this.futureDateValidator(), this.ageValidator(8, 100)]),
      age: new FormControl(''),
      about: new FormControl('', [Validators.minLength(3), Validators.maxLength(100)]),

      currentPass: new FormControl(''),
      newPass: new FormControl('', [Validators.minLength(6)]),
      confirmPass: new FormControl('')
    });
  }

  ngOnInit(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;

        let birthStr = '';
        if (user.birth) {
          birthStr = user.birth.includes('T')
            ? user.birth.substring(0, 10)
            : user.birth;
        }

        this.form.patchValue({
          name: user.name || '',
          nick: user.nick || '',
          email: user.email || '',
          phone: user.phone || '',
          about: user.about || '',
          birth: birthStr,
          age: birthStr ? this.calcAge(birthStr) : null
        });

        this.photoPreview = user.photoUrl || '';

        this.loading = false;
      },
      error: () => {
        this.msgError = 'No se pudo cargar tu perfil.';
        this.loading = false;
      }
    });

    this.form.get('birth')?.valueChanges.subscribe((birth: string) => {
      this.form.patchValue(
        { age: this.calcAge(birth) },
        { emitEvent: false }
      );
    });
  }


  public ageValidator = (minAge: number, maxAge: number): ValidatorFn => {
    return (control: AbstractControl): ValidationErrors | null => {

      const birthDateString = control.value;

      if (!birthDateString) {
        return null;
      }

      const age = this.calcAge(birthDateString);

      if (age === null) {
        return null;
      }

      if (age < minAge) {
        return { isUnderage: true };
      }

      if (age > maxAge) {
        return { isTooOld: true };
      }

      return null;
    };
  }

  public futureDateValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const dateValue = control.value;

      if (!dateValue) {
        return null;
      }

      const inputDate = new Date(dateValue);
      const today = new Date();

      today.setHours(0, 0, 0, 0);
      inputDate.setHours(0, 0, 0, 0);



      if (inputDate > today) {
        return { futureDate: true };
      }

      return null;
    };
  }


  get f() {
    return this.form.controls;
  }
  public get emailControl() {
    return this.form.get('email');
  }

  public get phoneControl() {
    return this.form.get('phone');
  }

  public get newPassControl() {
    return this.form.get('newPass');
  }

  public get aboutControl() {
    return this.form.get('about');
  }

  public get nickControl(){
    return this.form.get('nick');
  }

  calcAge(birth: string | Date): number | null {
    if (!birth) return null;
    const d = new Date(birth);
    if (isNaN(d.getTime())) return null;
    const t = new Date();
    let age = t.getFullYear() - d.getFullYear();
    const m = t.getMonth() - d.getMonth();
    if (m < 0 || (m === 0 && t.getDate() < d.getDate())) age--;
    return age >= 0 ? age : null;
  }

  getMinDate(): string {
    // Fecha mínima: hace 100 años desde hoy
    const today = new Date();
    const minDate = new Date(today.getFullYear() - 100, today.getMonth(), today.getDate());
    return minDate.toISOString().split('T')[0];
  }

  getMaxDate(): string {
    // Fecha máxima: hace 8 años desde hoy (edad mínima)
    const today = new Date();
    const maxDate = new Date(today.getFullYear() - 8, today.getMonth(), today.getDate());
    return maxDate.toISOString().split('T')[0];
  }

onSelectPhoto(event: any) {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = () => {
      this.photoPreview = reader.result as string;
      this.form.patchValue({ photo: this.photoPreview });
    };
    reader.readAsDataURL(file);
  }
}


removePhoto() {
  this.photoFile = undefined;
  this.photoPreview = this.DEFAULT_PHOTO;
  this.form.patchValue({ photo: this.photoPreview });
}


  passwordsMatch(): boolean {
    const newPass = this.form.get('newPass')?.value;
    const confirm = this.form.get('confirmPass')?.value;
    return !newPass || !confirm || newPass === confirm;
  }

  save() {
    this.submitted = true;
    this.msgError = undefined;
    this.msgOk = undefined;

    // Marcar todos los campos como tocados para mostrar los errores
    this.form.markAllAsTouched();

    if (!this.form.valid || !this.passwordsMatch()) {
      console.log('Formulario inválido');
      return;
    }

    // Asegurar que birth sea string en formato YYYY-MM-DD
    let birthValue = this.form.value.birth;
    let birthStr: string | null = null;

    if (birthValue) {
      if (typeof birthValue === 'string') {
        // Si es string, verificar si tiene timestamp
        if (birthValue.includes('T')) {
          // Extraer solo YYYY-MM-DD
          birthStr = birthValue.substring(0, 10);
        } else {
          birthStr = birthValue;
        }
      } else if (birthValue instanceof Date) {
        // Si es Date, convertir a YYYY-MM-DD
        birthStr = birthValue.toISOString().substring(0, 10);
      }
    }

    // Armar payload según UserUpdateDTO
    const payload: putResponse = {
      name: this.form.value.name,
      nick: this.form.value.nick,
      email: this.form.value.email,
      phone: this.form.value.phone,
      about: this.form.value.about,
      photoUrl: this.photoPreview || null,
      birth: birthStr,
      age: this.form.value.age,
      oldPassword: this.form.value.currentPass || null,
      newPassword: this.form.value.newPass || null,
      newNewPassword: this.form.value.confirmPass || null
    };


    console.log('Payload que se enviará al backend:', payload);

    this.userService.updateUser(payload).subscribe({
        next: (res: UpdateUserResponse) => {
          this.msgOk = 'Perfil actualizado con éxito.';

          this.errorMessages = "";

          if (res.newToken) {
            this.authService.setToken(res.newToken);
          }
        },
        error: (err) => {

          const backendError = err.error;

          if (backendError?.newToken) {
            this.authService.setToken(backendError.newToken);
          }

          this.msgError = backendError?.message ||
            (err.status === 401 ? 'Sesión expirada.' : 'Error al actualizar el perfil.');
        }
      });
  }

  cancel() {
    this.router.navigate(['/profile']);
  }

  logout() {
    this.authService.removeToken();
    this.router.navigate(['/login']);
  }


}
