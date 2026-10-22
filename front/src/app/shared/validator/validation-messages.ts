import { FieldNameType } from '@type/field-name.type';

export const VALIDATION_MESSAGES: Record<
  FieldNameType,
  Record<string, string>
> = {
  email: {
    required: "L'adresse e-mail est obligatoire.",
    email: "L'adresse e-mail n'est pas valide.",
    maxlength: "L'adresse e-mail ne doit pas dépasser 50 caractères.",
  },
  username: {
    required: "Le nom d'utilisateur est obligatoire.",
    minlength: 'Le nom d’utilisateur doit contenir au moins 3 caractères.',
    maxlength: 'Maximum 20 caractères.',
    usernameTaken: 'Ce nom d’utilisateur est déjà utilisé.',
    usernameCheckFailed:
      "Impossible de vérifier la disponibilité du nom d'utilisateur. Réessayez.",
  },
  password: {
    required: 'Le mot de passe est obligatoire.',
    minlength: 'Minimum 8 caractères.',
    uppercase: 'Au moins une lettre majuscule.',
    lowercase: 'Au moins une lettre minuscule.',
    number: 'Au moins un chiffre.',
    specialChar: 'Au moins un caractère spécial.',
  },
};
