import { FormControl, ValidationErrors } from '@angular/forms';
import { passwordValidator } from './password.validator';

describe('passwordValidator', () => {
  const validator = passwordValidator();

  function validate(password: string | null): ValidationErrors | null {
    return validator(new FormControl(password));
  }

  it('should return null when the password is empty', () => {
    expect(validate('')).toBeNull();
  });

  it('should return null when the password is null', () => {
    expect(validate(null)).toBeNull();
  });

  it('should return minlength error when the password has fewer than 8 characters', () => {
    expect(validate('Abc1!')).toEqual({
      minlength: true,
    });
  });

  it('should return uppercase error when the password has no uppercase letter', () => {
    expect(validate('abcdef1!')).toEqual({
      uppercase: true,
    });
  });

  it('should return lowercase error when the password has no lowercase letter', () => {
    expect(validate('ABCDEFG1!')).toEqual({
      lowercase: true,
    });
  });

  it('should return number error when the password has no digit', () => {
    expect(validate('Abcdefgh!')).toEqual({
      number: true,
    });
  });

  it('should return specialChar error when the password has no special character', () => {
    expect(validate('Abcdefg1')).toEqual({
      specialChar: true,
    });
  });

  it('should return multiple errors when several rules are not satisfied', () => {
    expect(validate('abcdefg')).toEqual({
      minlength: true,
      uppercase: true,
      number: true,
      specialChar: true,
    });
  });

  it('should return null when the password satisfies all rules', () => {
    expect(validate('Password1!')).toBeNull();
  });

  it('should accept a password with exactly 8 characters when all rules are satisfied', () => {
    expect(validate('Abcdef1!')).toBeNull();
  });

  it('should accept special characters other than punctuation', () => {
    expect(validate('Password1_')).toBeNull();
  });

  it('should not return minlength error when the password has exactly 8 characters', () => {
    expect(validate('Abcdef1!')).not.toEqual(
      jasmine.objectContaining({ minlength: true }),
    );
  });
});
