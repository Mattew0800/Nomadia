import { Component } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
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
  styleUrl: './user-profile-edit.css'
})
export class UserProfileEdit {

  form: FormGroup;
  submitted = false;
  photoFile?: File;
  photoPreview?: string;
  msgOk?: string;
  msgError?: string;
  loading = true;
  errorMessages: { [key: string]: string } = {};
  user?: User;
  DEFAULT_PHOTO = 'default-user-img.jpg';




  constructor(private http: HttpClient, private router: Router, public authService: AuthService, public userService: UserService) {

    this.form = new FormGroup({
      name: new FormControl('', [
        Validators.required,
        Validators.pattern(/^[A-Za-zÁÉÍÓÚáéíóúÑñ\s'.-]{2,}$/)
      ]),
      nick: new FormControl(''),
      email: new FormControl({ value: '', disabled: false }, [
        Validators.required,
        Validators.email
      ]),
      phone: new FormControl(''),
      birth: new FormControl('',[Validators.pattern(/^\d{4}-\d{2}-\d{2}$/)]),
      age: new FormControl(''),
      about: new FormControl(''),

      currentPass: new FormControl(''),
      newPass: new FormControl('', [Validators.minLength(6)]),
      confirmPass: new FormControl('')
    });
  }

  ngOnInit(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;

        // Convertir birth a YYYY-MM-DD si existe
        const birthStr = user.birth
          ? new Date(user.birth).toISOString().substring(0, 10)
          : '';

        // Patch del formulario con datos del usuario
        this.form.patchValue({
          name: user.name || '',
          nick: user.nick || '',
          email: user.email || '',
          phone: user.phone || '',
          about: user.about || '',
          birth: birthStr,
          age: birthStr ? this.calcAge(birthStr) : null
        });

        // Cargar la foto de perfil actual
        this.photoPreview = user.photoUrl || '';

        this.loading = false;
      },
      error: () => {
        this.msgError = 'No se pudo cargar tu perfil.';
        this.loading = false;
      }
    });

    // Suscribirse a cambios de la fecha de nacimiento para recalcular la edad
    this.form.get('birth')?.valueChanges.subscribe((birth: string) => {
      this.form.patchValue(
        { age: this.calcAge(birth) },
        { emitEvent: false } // evita loop de eventos
      );
    });
  }





  get f() {
    return this.form.controls;
  }

  calcAge(birth: string): number | null {
    if (!birth) return null;
    const d = new Date(birth);
    if (isNaN(d.getTime())) return null;
    const t = new Date();
    let age = t.getFullYear() - d.getFullYear();
    const m = t.getMonth() - d.getMonth();
    if (m < 0 || (m === 0 && t.getDate() < d.getDate())) age--;
    return age >= 0 ? age : null;
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

    if (!this.form.valid || !this.passwordsMatch()) {
      console.log('Formulario inválido');
      return;
    }

    // Convertimos birth a Date
    const birthDate = this.form.value.birth ? new Date(this.form.value.birth) : null;

    // Armar payload según UserUpdateDTO
    const payload: putResponse = {
      name: this.form.value.name,
      nick: this.form.value.nick,
      email: this.form.value.email,
      phone: this.form.value.phone,
      about: this.form.value.about,
      photoUrl: this.photoPreview || null,
      birth: birthDate,
      age: this.form.value.age,
      oldPassword: this.form.value.currentPass || null,
      newPassword: this.form.value.newPass || null,
      newNewPassword: this.form.value.confirmPass || null
    };


    console.log('Payload que se enviará al backend:', payload);

    this.userService.updateUser(payload).subscribe({
        next: (res: UpdateUserResponse) => {
          this.msgOk = 'Perfil actualizado con éxito.';

          this.errorMessages = {};


          if (res.newToken) {
            this.authService.setToken(res.newToken);
          }
        },
        error: (err) => {
          // CasTEA al nuevo tipo que incluye 'newToken'
          const backendError = err.error as AuthErrorResponse; // ⬅️ CORRECCIÓN AQUÍ

          // 1. Almacenar el nuevo token si se recibe
          if (backendError?.newToken) {
            this.authService.setToken(backendError.newToken);
          }

          // 2. Manejo de mensajes de error (el resto de la lógica sigue igual)
          if (backendError?.errors) {
            this.errorMessages = backendError.errors;
          } else {
            this.msgError =
              backendError?.message ||
              (err.status === 401
                ? 'Sesión expirada.'
                : 'Error al actualizar el perfil.');
          }
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
