describe('Register', () => {
  beforeEach(() => {
    cy.intercept('GET', '**/username-available*', {
      statusCode: 200,
      body: { available: true },
    }).as('usernameAvailable');

    cy.visit('/register');
  });

  const fillValidForm = () => {
    cy.get('#registerEmail').clear().type('newuser@example.com');
    cy.get('#registerUsername').clear().type('newuser123');
    cy.get('#registerPassword').clear().type('Password123!');
  };

  it('displays the registration page', () => {
    cy.url().should('include', '/register');
    cy.get('app-register').should('exist');
    cy.contains('h1', 'Inscription').should('be.visible');

    cy.get('#registerEmail').should('be.visible');
    cy.get('#registerUsername').should('be.visible');
    cy.get('#registerPassword').should('be.visible');

    cy.contains('button', 'S’inscrire').should('be.visible');
  });

  it('disables the button when the form is invalid', () => {
    cy.get('app-button.register__content__submit-button button').should(
      'be.disabled',
    );

    cy.get('#registerEmail').type('email-invalide');
    cy.get('#registerUsername').type('ab');
    cy.get('#registerPassword').type('abc');

    cy.get('app-button.register__content__submit-button button').should(
      'be.disabled',
    );
  });

  it('displays an error message if the email address is invalid', () => {
    cy.get('#registerEmail').type('email-invalide');
    cy.get('#registerEmail').blur();

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', "L'adresse e-mail n'est pas valide.");
  });

  it('displays an error when the username is too short', () => {
    cy.get('#registerUsername').type('ab');
    cy.get('#registerUsername').blur();

    cy.get('app-button.register__content__submit-button button').should(
      'be.disabled',
    );
  });

  it('displays an error message if the password is too short', () => {
    cy.get('#registerPassword').type('Ab1!');
    cy.get('#registerPassword').blur();

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', 'Minimum 8 caractères.');
  });

  it('displays an error if the username is already in use', () => {
    cy.intercept('GET', '**/username-available*', {
      statusCode: 200,
      body: { available: false },
    }).as('usernameUnavailable');

    cy.get('#registerUsername').type('existinguser');

    cy.wait('@usernameUnavailable');

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', 'Ce nom d’utilisateur est déjà utilisé.');

    cy.get('app-button.register__content__submit-button button').should(
      'be.disabled',
    );
  });

  it('displays an error if the username check fails', () => {
    cy.intercept('GET', '**/username-available*', {
      statusCode: 500,
      body: { message: 'Erreur serveur' },
    }).as('usernameCheckError');

    cy.get('#registerUsername').type('newuser123');

    cy.wait('@usernameCheckError');

    cy.get('mat-error')
      .should('be.visible')
      .and(
        'contain.text',
        "Impossible de vérifier la disponibilité du nom d'utilisateur. Réessayez.",
      );

    cy.get('app-button.register__content__submit-button button').should(
      'be.disabled',
    );
  });

  it('registers a user and redirects to the login page', () => {
    cy.intercept('POST', '**/api/v1/auth/register', {
      statusCode: 201,
      body: { message: 'Utilisateur créé' },
    }).as('register');

    fillValidForm();

    cy.get('app-button.register__content__submit-button button')
      .should('be.enabled')
      .click();

    cy.wait('@usernameAvailable');

    cy.wait('@register').its('response.statusCode').should('eq', 201);

    cy.url().should('include', '/login');
  });

  it('displays a message if the registration fails', () => {
    cy.intercept('GET', '**/api/v1/auth/username-available?*', {
      statusCode: 200,
      body: { available: true },
    }).as('usernameAvailable');

    cy.intercept('POST', '**/api/v1/auth/register', {
      statusCode: 409,
      body: { message: 'Cette adresse e-mail est déjà utilisée.' },
    }).as('registerError');

    fillValidForm();

    cy.wait('@usernameAvailable')
      .its('response.body.available')
      .should('eq', true);

    cy.get('app-button.register__content__submit-button button')
      .should('be.enabled')
      .click();

    cy.wait('@registerError').its('response.statusCode').should('eq', 409);

    cy.get('p.error[role="alert"]')
      .should('be.visible')
      .and('contain.text', "Une erreur est survenue lors de l'inscription.");

    cy.url().should('include', '/register');
  });

  it('Return to the home page by clicking the back button', () => {
    cy.get('button[aria-label="Retour à l\'accueil"]').click();

    cy.url().should('eq', `${Cypress.config('baseUrl')}/`);
  });
});
