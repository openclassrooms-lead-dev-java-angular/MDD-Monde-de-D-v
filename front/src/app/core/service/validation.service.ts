import { Injectable } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { VALIDATION_MESSAGES } from '@shared/validator/validation-messages';
import { FieldNameType } from '@type/field-name.type';

@Injectable({
  providedIn: 'root',
})
export class ValidationErrorService {
  getErrors(control: AbstractControl, fieldType: FieldNameType): string[] {
    if (!control.touched && !control.dirty) {
      return [];
    }

    if (!control.errors) {
      return [];
    }

    return Object.keys(control.errors)
      .map((errorKey) => VALIDATION_MESSAGES[fieldType][errorKey])
      .filter((message): message is string => message !== undefined);
  }
}
