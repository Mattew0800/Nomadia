import { NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
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
import { AuthService } from '../../services/Auth/auth-service';

@Component({
  selector: 'app-register-page',
  imports: [RouterLink, ReactiveFormsModule, NgClass],
  templateUrl: './register-page.html',
  styleUrl: './register-page.scss',
})
export class RegisterPage implements OnInit {

  registerForm: FormGroup;
  submitted = false;
  registerMsg?:string;
  errorMsg?:string;
  loading = true;
  invalidCredentials = false;
  showPassword = false;

  private imagesToPreload = [
    'coliseo-romano.jpg',
    'nomadia-penguin.png',
    'nomadia-logo.png'
  ];

  constructor(public authService: AuthService, private router: Router) {
      this.registerForm = new FormGroup({

          name: new FormControl('', [Validators.required,
            Validators.pattern(/^(?:[A-Za-zÁÉÍÓÚáéíóúÑñ]{2,}|de|del|la|los|san)(?:\s(?:[A-Za-zÁÉÍÓÚáéíóúÑñ]{2,}|de|del|la|los|san))+$/i),
            // Solo letras (con tildes/ñ), al menos dos palabras (espacio en medio)
            // Permite "de", "del", "la", "los", "san" | Sin simbolos o espacios extra.
            Validators.maxLength(50),
            this.maxWordsValidator(5)

          ]),

          email: new FormControl('', [
            Validators.required,
            Validators.pattern(/^(?!.*\.{2})[a-zA-Z0-9](?:[a-zA-Z0-9._%+-]*[a-zA-Z0-9])?@[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)*\.[a-zA-Z]{2,}$/),
            Validators.maxLength(254)
            //Prohíbe puntos consecutivos
            //Exige que el local-part no empiece/termine con punto
            //Controla que los guiones en el dominio no estén al inicio/final
            //Requiere TLD de al menos 2 letras
            //Permite subdominios
          ]),

          password: new FormControl('', [Validators.required, Validators.minLength(6), Validators.pattern(/\S/)]),

          terms: new FormControl(false, [Validators.requiredTrue])
      });
  }

  ngOnInit() {
    this.preloadImages();
  }

  private preloadImages() {
    const imagePromises = this.imagesToPreload.map(src => {
      return new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = () => resolve(src);
        img.onerror = () => reject(src);
        img.src = src;
      });
    });

    Promise.all(imagePromises)
      .then(() => {
        this.loading = false;
      })
      .catch((error) => {
        console.warn('Error cargando imagen:', error);
        this.loading = false;
      });
  }

    get nameC() {
      return this.registerForm.get('name');
    }

    get emailC() {
      return this.registerForm.get('email');
    }

    get passwordC() {
      return this.registerForm.get('password');
    }

    get termsC() {
      return this.registerForm.get('terms');
    }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }


  register() {
    this.submitted = true;

    if (this.registerForm.valid) {

      const { name, email, password } = this.registerForm.value;

      this.authService.registerUser(name, email, password).subscribe({
      next: () => {
        console.log("REGISTRADO JOYA");
        this.registerMsg="Registrado con éxito."

         setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);

      },
      error: (e) => {
        console.log(e);
        if (e.status === 409) {
          this.errorMsg = "El usuario ya existe.";
        } else {
          alert(e.error);
          this.errorMsg = "Ocurrió un error inesperado.";
        }
      }
      });

    } else {
      console.log('Formulario inválido');
      return;
    }
  }

  maxWordsValidator(max: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value || '';
      const words = value.trim().split(/\s+/).filter((w: string) => w.length > 0);
      return words.length > max ? { maxWords: { requiredMax: max, actual: words.length } } : null;
    };
  }

}
