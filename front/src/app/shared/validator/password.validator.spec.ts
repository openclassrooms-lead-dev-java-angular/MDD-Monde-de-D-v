import { FormControl } from '@angular/forms';

import { passwordValidator } from './password.validator';

describe('passwordValidator', () => {
  const validator = passwordValidator();

  it('should return null when the password is empty', () => {
    const control = new FormControl('');

    expect(validator(control)).toBeNull();
  });

  it('should return null when the password is null', () => {
    const control = new FormControl<string | null>(null);

    expect(validator(control)).toBeNull();
  });

  it('should return null when the password meets all requirements', () => {
    const control = new FormControl('StrongPass1!');

    expect(validator(control)).toBeNull();
  });

  it('should return minlength error when the password has fewer than 8 characters', () => {
    const control = new FormControl('Ab1!xyz');

    expect(validator(control)).toEqual({
      minlength: true,
    });
  });

  it('should return uppercase error when the password has no uppercase letter', () => {
    const control = new FormControl('strongpass1!');

    expect(validator(control)).toEqual({
      uppercase: true,
    });
  });

  it('should return lowercase error when the password has no lowercase letter', () => {
    const control = new FormControl('STRONGPASS1!');

    expect(validator(control)).toEqual({
      lowercase: true,
    });
  });

  it('should return number error when the password has no digit', () => {
    const control = new FormControl('StrongPass!');

    expect(validator(control)).toEqual({
      number: true,
    });
  });

  it('should return specialChar error when the password has no special character', () => {
    const control = new FormControl('StrongPass123');

    expect(validator(control)).toEqual({
      specialChar: true,
    });
  });

  it('should return all errors when the password fails every requirement', () => {
    const control = new FormControl('abc');

    expect(validator(control)).toEqual({
      minlength: true,
      uppercase: true,
      number: true,
      specialChar: true,
    });
  });

  it('should return multiple errors when the password fails multiple requirements', () => {
    const control = new FormControl('password');

    expect(validator(control)).toEqual({
      uppercase: true,
      number: true,
      specialChar: true,
    });
  });

  it('should accept a password with exactly 8 characters when all requirements are met', () => {
    const control = new FormControl('Abcdef1!');

    expect(validator(control)).toBeNull();
  });
});
