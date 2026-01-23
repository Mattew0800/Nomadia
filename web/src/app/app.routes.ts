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
import { UserProfileEdit } from './pages/user-profile-edit/user-profile-edit';
import { NewTravel } from './pages/new-travel/new-travel';
import { TripList } from './pages/trip-list/trip-list';
import {TripEdit} from './pages/edit-trip/edit-trip';
import {ActivityListComponent} from './pages/activity-list/activity-list';
import {ExpensesPage} from './pages/expenses-page/expenses-page';

export const routes: Routes = [
    {path: 'landing', component: LandingPage, canActivate: [PublicGuard]},
    {path: 'login', component: LoginPage, canActivate: [PublicGuard]},
    {path: 'register', component: RegisterPage, canActivate: [PublicGuard]},
    {path: 'mainPage', component:MainPage, canActivate: [AuthGuard]},
    {path: 'profile', component: UserProfile, canActivate: [AuthGuard]},
    {path: 'editProfile', component: UserProfileEdit, canActivate: [AuthGuard]},
    {path: 'newTrip', component: NewTravel, canActivate: [AuthGuard]},
    {path: 'tripList', component: TripList, canActivate: [AuthGuard]},
    { path: 'editTrip', component: TripEdit, canActivate: [AuthGuard] },
    {path: "activities", component: ActivityListComponent, canActivate: [AuthGuard]},
    {path: 'expenses', component: ExpensesPage, canActivate: [AuthGuard]},
    {path: 'test', component: Test},
    {path: 'error', component: ErrorPage},
    {path: '', redirectTo: 'landing', pathMatch: 'full'},
    {path: '**', redirectTo: 'error' },

];
