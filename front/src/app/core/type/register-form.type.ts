import { FormControl } from "@angular/forms";

export type RegisterForm = {
  email: FormControl<string>;
  username: FormControl<string>;
  password: FormControl<string>;
};
