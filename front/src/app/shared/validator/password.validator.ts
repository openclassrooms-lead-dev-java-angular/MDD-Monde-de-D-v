import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function passwordValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const password: string = control.value ?? '';
    const errors: ValidationErrors = {};

    if (!password) {
      return null;
    }

    if (password.length < 8) {
      errors['minlength'] = true;
    }

    if (!/[A-Z]/.test(password)) {
      errors['uppercase'] = true;
    }

    if (!/[a-z]/.test(password)) {
      errors['lowercase'] = true;
    }

    if (!/\d/.test(password)) {
      errors['number'] = true;
    }

    if (!/[^a-zA-Z0-9]/.test(password)) {
      errors['specialChar'] = true;
    }

    return Object.keys(errors).length > 0 ? errors : null;
  };
}
