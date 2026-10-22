describe('Connexion — Login', () => {
  beforeEach(() => {
    cy.visit('/login');
  });

  const fillValidForm = () => {
    cy.get('#usernameOrEmail').clear().type('testuser123');

    cy.get('#password').clear().type('TestPassword1!');
  };

  it('displays the login form', () => {
    cy.contains('h1', 'Connexion').should('be.visible');

    cy.get('#usernameOrEmail').should('be.visible');
    cy.get('#password').should('be.visible');

    cy.get('app-button button[type="submit"]').should('be.visible');
  });

  it('indicates which fields are compulsory when they are left blank', () => {
    cy.get('#usernameOrEmail').focus().blur();
    cy.get('#password').focus().blur();

    cy.contains(
      'mat-error',
      "L'email ou le nom d'utilisateur est obligatoire.",
    ).should('be.visible');

    cy.contains('mat-error', 'Le mot de passe est obligatoire.').should(
      'be.visible',
    );

    cy.get('app-button button[type="submit"]').should('be.disabled');
  });

  it('allows you to log in using valid credentials', () => {
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

    fillValidForm();

    cy.get('app-button button[type="submit"]')
      .should('not.be.disabled')
      .click();

    cy.wait('@login').then(({ request }) => {
      expect(request.body).to.deep.equal({
        usernameOrEmail: 'testuser123',
        password: 'TestPassword1!',
      });
    });

    cy.wait('@getMe');

    cy.location('pathname').should('eq', '/feed');
  });

  it('displays a message if the login details are incorrect', () => {
    cy.intercept('POST', '**/api/v1/auth/login', {
      statusCode: 401,
      body: { message: 'Identifiants incorrects' },
    }).as('loginError');

    cy.visit('/login');

    cy.get('#usernameOrEmail').type('testuser123');
    cy.get('#password').type('TestPassword1!');

    cy.get('app-button.login__content__submit-button button').click();

    cy.wait('@loginError');

    cy.get('app-login').should(($component) => {
      expect($component.find('p.error[role="alert"]').length).to.equal(1);
    });

    cy.get('p.error[role="alert"]')
      .should('be.visible')
      .and(
        'contain.text',
        'Erreur lors de la connexion. Vérifiez vos identifiants et réessayez.',
      );
  });

  it('Return to the home page using the back button', () => {
    cy.get('button[aria-label="Retour à l\'accueil"]').click();

    cy.location('pathname').should('eq', '/');
  });
});
