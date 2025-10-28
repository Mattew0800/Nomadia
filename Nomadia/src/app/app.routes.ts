import { Routes } from '@angular/router';
import { LandingPage } from './pages/landing-page/landing-page';
import { LoginPage } from './pages/login-page/login-page';
import { RegisterPage } from './pages/register-page/register-page';
import { ErrorPage } from './pages/error-page/error-page';
import { UserProfile } from './pages/user-profile/user-profile';
import { Test } from './pages/test/test';
import { AuthGuard } from './guards/auth.guard';
import { PublicGuard } from './guards/public.guard';
import { MainPage } from './pages/main-page/main-page';

export const routes: Routes = [
    {path: 'landing', component: LandingPage},
    {path: 'login', component: LoginPage, canActivate: [PublicGuard]},
    {path: 'register', component: RegisterPage, canActivate: [PublicGuard]},
    {path: 'mainPage', component: MainPage},
    {path: 'error', component: ErrorPage},
    {path: 'profile', component: UserProfile, canActivate: [AuthGuard]},
    {path: 'test', component: Test},
    {path: '', redirectTo: 'landing', pathMatch: 'full'},
    {path: '**', redirectTo: 'error' },
];
