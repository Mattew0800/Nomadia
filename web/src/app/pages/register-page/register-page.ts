import { NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
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

  private imagesToPreload = [
    'coliseo-romano.jpg',
    'nomadia-penguin.png',
    'nomadia-logo.png'
  ];

  constructor(public authService: AuthService, private router: Router) {
      this.registerForm = new FormGroup({

          name: new FormControl('', [Validators.required,
            Validators.pattern(/^(?:[A-Za-zÁÉÍÓÚáéíóúÑñ]{2,}|de|del|la|los|san)(?:\s(?:[A-Za-zÁÉÍÓÚáéíóúÑñ]{2,}|de|del|la|los|san))+$/i)
            // Solo letras (con tildes/ñ), al menos dos palabras (espacio en medio)
            // Permite "de", "del", "la", "los", "san" | Sin simbolos o espacios extra.
          ]),

          email: new FormControl('', [Validators.required, Validators.email]),

          password: new FormControl('', [Validators.required, Validators.minLength(6)]),

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
          this.errorMsg = "Ocurrió un error inesperado.";
        }
      }
      });

    } else {
      console.log('Formulario inválido');
      return;
    }
  }

}
