import { Routes } from '@angular/router';
import { LandingPage } from './pages/landing-page/landing-page';
import { LoginPage } from './pages/login-page/login-page';
import { RegisterPage } from './pages/register-page/register-page';
import { ErrorPage } from './pages/error-page/error-page';
import { UserProfile } from './pages/user-profile/user-profile';
import { Test } from './pages/test/test';

export const routes: Routes = [
    {path: 'landing', component: LandingPage},
    {path: 'login', component: LoginPage},
    {path: 'register', component: RegisterPage},
    {path: 'error', component: ErrorPage},
    {path: 'profile', component: UserProfile},
    {path: 'test', component: Test},
    {path: '', redirectTo: 'landing', pathMatch: 'full'},
    {path: '**', redirectTo: 'error' },
    
];
