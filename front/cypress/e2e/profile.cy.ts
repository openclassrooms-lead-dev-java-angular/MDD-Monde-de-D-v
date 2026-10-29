describe('Profile page', () => {
  const currentUser = {
    email: 'test@example.com',
    username: 'testuser',
    createdAt: new Date('2025-01-01T10:00:00Z'),
    updatedAt: new Date('2025-01-01T10:00:00Z'),
  };

  const topic = {
    publicId: 'angular',
    name: 'Angular',
    description: 'Découvrez Angular.',
    subscribed: true,
    createdAt: new Date('2025-01-01T10:00:00Z'),
    updatedAt: new Date('2025-01-01T10:00:00Z'),
  };

  const createPage = (content: (typeof topic)[], number = 0, last = true) => ({
    content,
    number,
    size: 10,
    totalElements: content.length,
    totalPages: last ? 1 : 2,
    first: number === 0,
    last,
    empty: content.length === 0,
  });

  const interceptProfileRequests = (topicsPage = createPage([], 0, true)) => {
    cy.intercept('GET', '**/api/v1/users/me', {
      statusCode: 200,
      body: currentUser,
    }).as('getCurrentUser');

    cy.intercept('GET', '**/api/v1/users/username-available*', {
      statusCode: 200,
      body: { available: true },
    }).as('checkUsername');

    cy.intercept('GET', '**/api/v1/topics/subscribed*', {
      statusCode: 200,
      body: topicsPage,
    }).as('getSubscribedTopics');
  };

  const visitProfile = (topicsPage = createPage([], 0, true)) => {
    interceptProfileRequests(topicsPage);
    cy.visit('/profile');
    cy.wait('@getSubscribedTopics');
  };

  const fillValidForm = () => {
    cy.get('#registerEmail').clear().type('updated@example.com');
    cy.get('#registerUsername').clear().type('updateduser');
    cy.get('#registerPassword').clear().type('Password123!');
  };

  beforeEach(() => {
    cy.login();
  });

  it('should display the profile page and current user information', () => {
    visitProfile();

    cy.get('#registerEmail').should('have.value', currentUser.email);
    cy.get('#registerUsername').should('have.value', currentUser.username);
    cy.get('#registerPassword').should('exist');

    cy.contains('h1', 'Profil utilisateur').should('be.visible');
    cy.contains('h2', 'Abonnements').should('be.visible');
    cy.contains('button', 'Sauvegarder').should('be.visible');
  });

  it('should update the profile successfully', () => {
    cy.intercept('PATCH', '**/api/v1/users/me', {
      statusCode: 200,
      body: {
        ...currentUser,
        email: 'updated@example.com',
        username: 'updateduser',
      },
    }).as('updateProfile');

    visitProfile();
    fillValidForm();

    cy.get('#registerUsername').should('have.value', 'updateduser');
    cy.wait('@checkUsername');

    cy.contains('button', 'Sauvegarder').click();

    cy.wait('@updateProfile').then(({ request }) => {
      expect(request.body).to.deep.include({
        email: 'updated@example.com',
        username: 'updateduser',
        password: 'Password123!',
      });
    });

    cy.get('.profile__error').should('not.exist');
  });

  it('should display an error when the profile update fails', () => {
    cy.intercept('PATCH', '**/api/v1/users/me', {
      statusCode: 500,
      body: { message: 'Server error' },
    }).as('updateProfileError');

    visitProfile();
    fillValidForm();

    cy.wait('@checkUsername');
    cy.contains('button', 'Sauvegarder').click();

    cy.wait('@updateProfileError');

    cy.get('.profile__error')
      .should('be.visible')
      .and('contain.text', "Une erreur est survenue lors de l'inscription.");
  });

  it('should display validation errors for an invalid email', () => {
    visitProfile();

    cy.get('#registerEmail').clear().type('invalid-email');
    cy.get('#registerUsername').click();

    cy.get('#registerEmail').should('have.value', 'invalid-email');
    cy.get('mat-error').should('exist');
  });

  it('should display validation errors when the password is empty', () => {
    visitProfile();

    cy.get('#registerPassword').clear().blur();

    cy.get('mat-error').should('exist');
  });

  it('should display an empty state when there are no subscribed topics', () => {
    visitProfile();

    cy.contains('.profile__empty', "Vous n'êtes abonné à aucun thème.").should(
      'be.visible',
    );

    cy.get('app-topic-card').should('not.exist');
  });

  it('should display subscribed topics', () => {
    visitProfile(createPage([topic]));

    cy.get('app-topic-card').should('have.length', 1);
    cy.contains('Angular').should('be.visible');
  });

  it('should display the loading indicator while subscribed topics are loading', () => {
    cy.intercept('GET', '**/api/v1/topics/subscribed*', {
      statusCode: 200,
      delay: 1000,
      body: createPage([topic]),
    }).as('slowGetSubscribedTopics');

    cy.visit('/profile');

    cy.get('[aria-label="Chargement des abonnements"]').should('be.visible');

    cy.wait('@slowGetSubscribedTopics');

    cy.get('[aria-label="Chargement des abonnements"]').should('not.exist');
  });
});
