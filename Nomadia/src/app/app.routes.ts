import { Routes } from '@angular/router';
import { LandingPage } from './pages/landing-page/landing-page';
import { LoginPage } from './pages/login-page/login-page';
import { RegisterPage } from './pages/register-page/register-page';
import { ErrorPage } from './pages/error-page/error-page';

export const routes: Routes = [
    {path: 'landing', component: LandingPage},
    {path: 'login', component: LoginPage},
    {path: 'register', component: RegisterPage},
    {path: 'error', component: ErrorPage},
    {path: '', redirectTo: 'landing', pathMatch: 'full'},
    {path: '**', redirectTo: 'error' },
    
];
