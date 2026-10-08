import { Routes } from '@angular/router';
import { LoginComponent } from '@pages/auth/login/login.component';
import { RegisterComponent } from '@pages/auth/register/register.component';
import { HomeComponent } from '@pages/home/home.component';
import { NotFoundComponent } from '@pages/not-found/not-found.component';
import { TopicComponent } from '@pages/topic/topic.component';
import { ProfileComponent } from '@pages/profile/profile.component';
import { AuthGuard } from './core/guard/auth.guard';
import { FeedComponent } from '@pages/feed/feed.component';
import { DefaultLayoutComponent } from './layouts/default-layout/default-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: DefaultLayoutComponent,
    children: [
      {
        path: '',
        component: HomeComponent,
      },
      {
        path: 'register',
        component: RegisterComponent,
      },
      {
        path: 'login',
        component: LoginComponent,
      },
      {
        path: 'topics',
        component: TopicComponent,
        canActivate: [AuthGuard],
      },
      {
        path: 'feed',
        component: FeedComponent,
        canActivate: [AuthGuard],
      },
      {
        path: 'profile',
        component: ProfileComponent,
        canActivate: [AuthGuard],
      },
    ],
  },
  { path: '404', component: NotFoundComponent },
  {
    path: '**',
    redirectTo: '404',
  },
];
