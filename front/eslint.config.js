const eslint = require('@eslint/js');
const angular = require('@angular-eslint/eslint-plugin');
const angularTemplate = require('@angular-eslint/eslint-plugin-template');
const angularTemplateParser = require('@angular-eslint/template-parser');
const tsParser = require('@typescript-eslint/parser');
const globals = require('globals');

module.exports = [
  {
    ignores: ['dist/**', 'coverage/**', 'node_modules/**', '.angular/**'],
  },

  eslint.configs.recommended,

  {
    files: ['**/*.ts'],
    languageOptions: {
      parser: tsParser,
      globals: {
        ...globals.browser,
        ...globals.jasmine,
      },
      parserOptions: {
        projectService: true,
      },
    },
    plugins: {
      '@angular-eslint': angular,
    },
    rules: {
      ...eslint.configs.recommended.rules,
      '@angular-eslint/directive-class-suffix': 'error',
      '@angular-eslint/component-class-suffix': 'error',
    },
  },

  {
    files: ['**/*.html'],
    languageOptions: {
      parser: angularTemplateParser,
    },
    plugins: {
      '@angular-eslint/template': angularTemplate,
    },
    rules: {},
  },

  {
    files: ['cypress/e2e/**/*.cy.ts', 'cypress/**/*.ts'],
    languageOptions: {
      globals: {
        ...globals.cypress,
        cy: 'readonly',
        Cypress: 'readonly',
        expect: 'readonly',
      },
    },
  },

  {
    files: ['cypress/**/*.ts'],
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.mocha,
        cy: 'readonly',
        Cypress: 'readonly',
      },
    },
  },

  {
    files: ['cypress/**/*.ts'],
    rules: {
      '@typescript-eslint/no-unused-vars': 'off',
    },
  },
];
