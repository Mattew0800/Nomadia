import { Component } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-user-profile-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NgClass, HttpClientModule],
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

  constructor(private http: HttpClient, private router: Router, public authService: AuthService) {

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
      birth: new FormControl(''),
      age: new FormControl(''),
      about: new FormControl(''),

      currentPass: new FormControl(''),
      newPass: new FormControl('', [Validators.minLength(6)]),
      confirmPass: new FormControl('')
    });
  }

  ngOnInit(): void {
    // Cargar datos del perfil actual
    this.http.get<any>('/api/users/me').subscribe({
      next: (user) => {
        this.form.patchValue(user);
        this.photoPreview = user.photoUrl;
        this.loading = false;
      },
      error: () => {
        this.msgError = 'No se pudo cargar tu perfil.';
        this.loading = false;
      }
    });

    // Calcular edad al cambiar birth
    this.form.get('birth')?.valueChanges.subscribe(birth => {
      this.form.patchValue({ age: this.calcAge(birth) }, { emitEvent: false });
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
    this.photoPreview = undefined;
  }

  passwordsMatch(): boolean {
    const newPass = this.form.get('newPassword')?.value;
    const confirm = this.form.get('confirmPassword')?.value;
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

    const raw = this.form.getRawValue();
    const fd = new FormData();

    fd.append('name', raw.name);
    if (raw.nick) fd.append('nick', raw.nick);
    if (raw.phone) fd.append('phone', raw.phone);
    if (raw.birth) fd.append('birth', raw.birth);
    if (raw.age) fd.append('age', raw.age.toString());
    if (raw.about) fd.append('about', raw.about);

    if (this.photoFile) fd.append('photo', this.photoFile);

    if (raw.currentPass && raw.newPass) {
      fd.append('currentPass', raw.currentPass);
      fd.append('newPassword', raw.newPass);
    }

    const payload = {
      name: this.form.value.name,
      nick: this.form.value.nick,
      email: this.form.value.email,
      phone: this.form.value.phone,
      birth: this.form.value.birth,
      age: this.form.value.age,
      about: this.form.value.about,
      currentPass: this.form.value.currentPass,
      newPass: this.form.value.newPass,
      confirmPass: this.form.value.confirmPass,
      photo: this.form.value.photo
    };

    this.authService.updateUser(payload).subscribe({
      next: () => {
        this.msgOk = 'Perfil actualizado con éxito.';
        //setTimeout(() => this.router.navigate(['/profile']), 1200);
      },
      error: (e) => {
        console.error(e);
        this.msgError = e.status === 400 ? 'Datos inválidos.' :
                        e.status === 401 ? 'Sesión expirada.' :
                        'Error al actualizar el perfil.';
      }
    });
  }

  cancel() {
    this.router.navigate(['/profile']);
  }

  logout() {
    localStorage.removeItem('token');
    this.authService.users = [];
    this.router.navigate(['/login']);
  }

}
