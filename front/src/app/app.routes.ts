import { Routes } from "@angular/router";
import { RegisterComponent } from "@pages/auth/register/register.component";
import { HomeComponent } from "@pages/home/home.component";

export const routes: Routes = [
    {
        path: '',
        component: HomeComponent,
    },
    {
        path: 'register',
        component: RegisterComponent,
    },
    // {
    //     path: 'login',
    //     component: LoginComponent,
    // },
    // {
    //     path: '**',
    //     redirectTo: NotFoundComponent,
    // }
];