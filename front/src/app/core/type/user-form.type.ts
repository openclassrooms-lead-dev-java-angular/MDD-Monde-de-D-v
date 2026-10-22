import { FormControl } from '@angular/forms';

export type UserFormType = {
  email: FormControl<string | null>;
  username: FormControl<string | null>;
  password: FormControl<string | null>;
};
