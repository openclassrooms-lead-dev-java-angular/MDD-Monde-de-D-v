/// <reference types="cypress" />
// ***********************************************
// This example commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
//
// declare global {
//   namespace Cypress {
//     interface Chainable {
//       login(email: string, password: string): Chainable<void>
//       drag(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       dismiss(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       visit(originalFn: CommandOriginalFn, url: string, options: Partial<VisitOptions>): Chainable<Element>
//     }
//   }
// }

declare global {
  namespace Cypress {
    interface Chainable {
      login(): Chainable<void>;
    }
  }
}

export {};

Cypress.Commands.add('login', () => {
  cy.intercept('POST', '**/login', {
    statusCode: 200,
    body: null,
  }).as('login');

  cy.intercept('GET', '**/me', {
    statusCode: 200,
    body: {
      email: 'test.e2e@example.com',
      username: 'testuser123',
      createdAt: '2026-01-01T10:00:00.000Z',
      updatedAt: '2026-01-01T10:00:00.000Z',
    },
  }).as('getMe');

  cy.visit('/login');

  cy.get('input').then(() => {
    cy.get('input[type="text"]').type('testuser123');
    cy.get('input[type="password"]').type('TestPassword1!');
  });

  cy.get('app-button button[type="submit"]').should('not.be.disabled').click();

  cy.wait('@login');
  cy.wait('@getMe');
});
