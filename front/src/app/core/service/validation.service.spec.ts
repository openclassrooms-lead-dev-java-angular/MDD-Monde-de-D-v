
import { TestBed } from '@angular/core/testing';
import {  FormControl, Validators } from '@angular/forms';
import { ValidationErrorService } from './validation.service';
import { FieldNameType } from '@type/field-name.type';
import { VALIDATION_MESSAGES } from '@shared/validator/validation-messages';


describe('ValidationErrorService', () => {
  let service: ValidationErrorService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ValidationErrorService],
    });

    service = TestBed.inject(ValidationErrorService);
  });

  describe('getErrors', () => {
    const fieldType = 'email' as FieldNameType;

    it('should return an empty array when the control is untouched and pristine', () => {
      const control = new FormControl('', Validators.required);

      const result = service.getErrors(control, fieldType);

      expect(result).toEqual([]);
    });

    it('should return an empty array when the control has no errors', () => {
      const control = new FormControl('valid@example.com');

      control.markAsTouched();

      const result = service.getErrors(control, fieldType);

      expect(result).toEqual([]);
    });

    it('should return the corresponding validation message when the control is touched', () => {
      const control = new FormControl('', Validators.required);

      control.markAsTouched();

      const result = service.getErrors(control, fieldType);

      expect(result).toContain(VALIDATION_MESSAGES[fieldType]['required']);
    });

    it('should return the corresponding validation message when the control is dirty', () => {
      const control = new FormControl('', Validators.required);

      control.markAsDirty();

      const result = service.getErrors(control, fieldType);

      expect(result).toContain(VALIDATION_MESSAGES[fieldType]['required']);
    });

    it('should return an empty array when the control has errors but is untouched and pristine', () => {
      const control = new FormControl('', Validators.required);

      expect(control.errors).not.toBeNull();

      const result = service.getErrors(control, fieldType);

      expect(result).toEqual([]);
    });

    it('should ignore validation errors without a corresponding message', () => {
      const control = new FormControl('');

      control.setErrors({
        unknownError: true,
      });
      control.markAsTouched();

      const result = service.getErrors(control, fieldType);

      expect(result).toEqual([]);
    });

    it('should return only messages defined in VALIDATION_MESSAGES', () => {
      const control = new FormControl('');

      control.setErrors({
        required: true,
        unknownError: true,
      });
      control.markAsTouched();

      const result = service.getErrors(control, fieldType);

      expect(result).toEqual(
        VALIDATION_MESSAGES[fieldType]['required']
          ? [VALIDATION_MESSAGES[fieldType]['required']]
          : [],
      );
    });

    it('should return multiple validation messages when multiple errors exist', () => {
      const control = new FormControl('');

      control.setErrors({
        required: true,
        email: true,
      });
      control.markAsTouched();

      const result = service.getErrors(control, fieldType);

      const expectedMessages = ['required', 'email']
        .map((key) => VALIDATION_MESSAGES[fieldType][key])
        .filter((message): message is string => message !== undefined);

      expect(result).toEqual(expectedMessages);
    });
  });
});
