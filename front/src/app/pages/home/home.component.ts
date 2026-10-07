import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [MatButtonModule, ButtonComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent {}
