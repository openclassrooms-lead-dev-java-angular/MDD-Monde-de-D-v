import { Component, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';

export type ButtonType = 'button' | 'link' | 'submit';
export type ButtonVariant = 'primary' | 'secondary' | 'outlined';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [MatButtonModule, RouterLink],
  templateUrl: './button.component.html',
  styleUrl: './button.component.scss',
})
export class ButtonComponent {
  readonly type = input<ButtonType>('button');
  readonly disabled = input<boolean>(false);
  readonly label = input.required<string>();
  readonly routerLink = input<string>();
  readonly alt = input<string>();
  readonly variant = input<ButtonVariant>();
  readonly outlined = input<boolean>(false);
  readonly action = input<(() => void) | undefined>();
  readonly fontWeight = input<number>(600);

  variantClass(): string {
    return this.variant ? `button-${this.variant}` : '';
  }
}
