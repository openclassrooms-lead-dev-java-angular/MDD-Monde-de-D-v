import { FormControl } from '@angular/forms';

export type LoginForm = {
  usernameOrEmail: FormControl<string>;
  password: FormControl<string>;
};
