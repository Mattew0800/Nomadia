import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../../services/auth-service';


/** Ajustá este servicio a tu firma real; acá muestro ejemplo básico */
type UserProfile = {
  id: number;
  name: string;
  nick?: string;
  email: string;
  phone?: string;
  birth?: string;   // ISO yyyy-MM-dd
  age?: number;
  about?: string;
  photoUrl?: string;
};

function samePassword(ctrl: AbstractControl): ValidationErrors | null {
  const newPass = ctrl.get('newPassword')?.value;
  const confirm = ctrl.get('confirmPassword')?.value;
  if (!newPass && !confirm) return null;
  return newPass === confirm ? null : { mismatch: true };
}

@Component({
  selector: 'app-user-profile-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NgClass, HttpClientModule],
  templateUrl: './user-profile-edit.html',
  styleUrls: ['./user-profile-edit.css']
})
export class UserProfileEdit implements OnInit {

  private http = inject(HttpClient);
  private router = inject(Router);

  loading = signal<boolean>(true);
  saving  = signal<boolean>(false);
  apiError = signal<string | null>(null);
  apiOk    = signal<string | null>(null);

  form!: FormGroup<{
    name: FormControl<string>;
    nick: FormControl<string | null>;
    email: FormControl<string>;
    phone: FormControl<string | null>;
    birth: FormControl<string | null>;
    age: FormControl<number | null>;
    about: FormControl<string | null>;
    passwordGroup: FormGroup<{
      currentPassword: FormControl<string | null>;
      newPassword: FormControl<string | null>;
      confirmPassword: FormControl<string | null>;
    }>
  }>;

  photoPreview = signal<string | null>(null);
  photoFile: File | null = null;

  constructor(public authService: AuthService, router: Router){}

  ngOnInit(): void {
    // Traer perfil actual (ajustá a tu endpoint real; usando proxy /api)
    this.http.get<UserProfile>('/api/users/me').subscribe({
      next: (user) => { this.buildForm(user); this.loading.set(false); },
      error: () => { this.apiError.set('No se pudo cargar tu perfil.'); this.loading.set(false); }
    });
  }

  private buildForm(user: UserProfile) {
    this.form = new FormGroup({
      name: new FormControl(user.name ?? '', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(/^[\p{L}\s'.-]{2,}$/u)]
      }),
      nick: new FormControl(user.nick ?? null),
      email: new FormControl({ value: user.email ?? '', disabled: true }, { nonNullable: true, validators: [Validators.required, Validators.email] }),
      phone: new FormControl(user.phone ?? null),
      birth: new FormControl(user.birth ?? null),
      age: new FormControl(user.age ?? null),
      about: new FormControl(user.about ?? null),
      passwordGroup: new FormGroup({
        currentPassword: new FormControl<string | null>(null),
        newPassword: new FormControl<string | null>(null, [Validators.minLength(6)]),
        confirmPassword: new FormControl<string | null>(null)
      }, { validators: samePassword })
    });

    // Recalcular edad cuando cambia fecha
    this.form.get('birth')!.valueChanges.subscribe(v => {
      this.form.get('age')!.setValue(this.calcAge(v ?? undefined), { emitEvent: false });
    });

    if (user.photoUrl) this.photoPreview.set(user.photoUrl);
  }

  private calcAge(birth?: string | null): number | null {
    if (!birth) return null;
    const d = new Date(birth);
    if (Number.isNaN(d.getTime())) return null;
    const t = new Date();
    let age = t.getFullYear() - d.getFullYear();
    const m = t.getMonth() - d.getMonth();
    if (m < 0 || (m === 0 && t.getDate() < d.getDate())) age--;
    return Math.max(age, 0);
  }

  onSelectPhoto(ev: Event) {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.photoFile = file;
    const reader = new FileReader();
    reader.onload = () => this.photoPreview.set(reader.result as string);
    reader.readAsDataURL(file);
  }

  removePhoto() {
    this.photoFile = null;
    this.photoPreview.set(null);
  }

  save() {
    this.apiError.set(null);
    this.apiOk.set(null);
    if (!this.form.valid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);

    const raw = this.form.getRawValue();
    const fd = new FormData();
    fd.append('name', raw.name);
    if (raw.nick)  fd.append('nick', raw.nick);
    if (raw.phone) fd.append('phone', raw.phone);
    if (raw.birth) fd.append('birth', raw.birth);
    if (raw.age !== null && raw.age !== undefined) fd.append('age', String(raw.age));
    if (raw.about) fd.append('about', raw.about);
    if (this.photoFile) fd.append('photo', this.photoFile);

    const pass = raw.passwordGroup;
    const wantsPassChange = pass.currentPassword && pass.newPassword;
    if (wantsPassChange) {
      fd.append('currentPassword', pass.currentPassword!);
      fd.append('newPassword', pass.newPassword!);
    }

    // Ajustá a tu endpoint (ej: /api/users/me  PUT/PATCH; si subís foto, muchos back usan multipart)
    this.http.put('/api/users/me', fd).subscribe({
      next: () => { this.saving.set(false); this.apiOk.set('Perfil actualizado.'); setTimeout(() => this.router.navigate(['/profile']), 900); },
      error: (e) => {
        this.saving.set(false);
        if (e?.status === 400) this.apiError.set('Datos inválidos.');
        else if (e?.status === 401) this.apiError.set('Sesión expirada. Iniciá sesión de nuevo.');
        else this.apiError.set('No se pudo actualizar el perfil.');
      }
    });
  }

  cancel() {
    this.router.navigate(['/user-profile']);
  }

    logout() {
      localStorage.removeItem('token');
      this.authService.users = [];
      this.router.navigate(['/login']);
    }
}
