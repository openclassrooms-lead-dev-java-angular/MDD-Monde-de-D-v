import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from 'src/app/shared/components/navbar/navbar.component';

@Component({
  selector: 'app-default-layout',
  standalone: true,
  imports: [NavbarComponent, RouterOutlet],
  templateUrl: './default-layout.component.html',
})
export class DefaultLayoutComponent {}
