import { NgClass } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-register-page',
  imports: [RouterLink, ReactiveFormsModule, NgClass],
  templateUrl: './register-page.html',
  styleUrl: './register-page.css',
})
export class RegisterPage {  

  registerForm: FormGroup;
  submitted = false;
  registerMsg?:string;

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
      }
      });

    } else {
      console.log('Formulario inválido');
      return;
    }
  }

}
