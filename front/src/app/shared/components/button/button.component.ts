import { Component, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';

type ButtonType = 'button' | 'link' | 'submit';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [MatButtonModule],
  templateUrl: './button.component.html',
  styleUrl: './button.component.scss',
})
export class ButtonComponent {
  readonly type = input<ButtonType>('button');
  readonly disabled = input<boolean>(false);
  readonly label = input.required<string>();
  readonly routerLink = input<string>();
  readonly alt = input<string>();
  readonly backgroundColor = input<string>();
  readonly outlined = input<boolean>(false);
  readonly action = input<(() => void) | undefined>();
  readonly fontStyle = input<string>('semibold');
}
