const eslint = require("@eslint/js");
const angular = require("@angular-eslint/eslint-plugin");
const angularTemplate = require("@angular-eslint/eslint-plugin-template");
const angularTemplateParser = require("@angular-eslint/template-parser");

module.exports = [
  {
    ignores: ["dist/**", "coverage/**", "node_modules/**", ".angular/**"],
  },

  eslint.configs.recommended,

  {
    files: ["**/*.ts"],
    plugins: {
      "@angular-eslint": angular,
    },
    languageOptions: {
      parserOptions: {
        projectService: true,
      },
    },
    rules: {
      "@angular-eslint/directive-class-suffix": "error",
      "@angular-eslint/component-class-suffix": "error",
    },
  },

  {
    files: ["**/*.html"],
    languageOptions: {
      parser: angularTemplateParser,
    },
    plugins: {
      "@angular-eslint/template": angularTemplate,
    },
    rules: {
      ...angularTemplate.configs.recommended.rules,
    },
  },
];
