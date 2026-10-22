describe('Profile', () => {
  const currentUser = {
    id: 1,
    email: 'test@example.com',
    username: 'testuser',
  };

  const subscribedTopic = {
    id: 1,
    name: 'Angular',
    slug: 'angular',
    description: 'Everything about Angular',
  };

  const emptyPage = {
    content: [],
    number: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    last: true,
  };

  beforeEach(() => {
    // Mock the current user and subscribed topics.
    cy.intercept('GET', '**/api/v1/users/me', {
      statusCode: 200,
      body: currentUser,
    }).as('getCurrentUser');

    cy.intercept('GET', '**/username-available*', {
      statusCode: 200,
      body: { available: true },
    }).as('usernameAvailable');

    cy.intercept('GET', '**/api/v1/topics/subscribed*', {
      statusCode: 200,
      body: emptyPage,
    }).as('getSubscribedTopics');

    // Visit the profile page.
    cy.visit('/profile');

    cy.wait('@getSubscribedTopics');
  });

  const fillValidForm = () => {
    cy.get('#registerEmail').clear().type('updated@example.com');
    cy.get('#registerUsername').clear().type('updateduser');
    cy.get('#registerPassword').clear().type('Password123!');
  };

  it('should display the profile page', () => {
    cy.get('app-profile').should('exist');
    cy.contains('h1', 'Profil utilisateur').should('be.visible');

    cy.get('#registerEmail')
      .should('be.visible')
      .and('have.value', currentUser.email);

    cy.get('#registerUsername')
      .should('be.visible')
      .and('have.value', currentUser.username);

    cy.get('#registerPassword').should('be.visible');

    cy.contains('h2', 'Abonnements').should('be.visible');
  });

  it('should update the profile successfully', () => {
    cy.intercept('GET', '**/api/v1/auth/username-available*', {
      statusCode: 200,
      body: { available: true },
    }).as('usernameAvailable');

    cy.intercept('PUT', '**/api/v1/users/me', {
      statusCode: 200,
      body: {
        ...currentUser,
        email: 'updated@example.com',
        username: 'updateduser',
      },
    }).as('updateProfile');

    fillValidForm();

    // Wait for the availability check for the updated username.
    cy.wait('@usernameAvailable', {
      requestTimeout: 10000,
    }).then(() => {
      cy.wait('@usernameAvailable', {
        requestTimeout: 10000,
      }).then((interception) => {
        expect(interception.request.url).to.include('username=updateduser');
      });
    });

    // Wait until the asynchronous validation has finished.
    cy.get('#registerUsername').should(($input) => {
      expect($input.attr('aria-busy')).not.to.equal('true');
    });

    // Submit the valid profile form.
    cy.get('app-button button')
      .should('contain.text', 'Sauvegarder')
      .and('be.enabled')
      .click();

    cy.wait('@updateProfile').its('request.body').should('deep.equal', {
      email: 'updated@example.com',
      username: 'updateduser',
      password: 'Password123!',
    });
  });

  it('should display an error when the email is empty', () => {
    cy.get('#registerEmail').clear().blur();

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', "L'adresse e-mail est obligatoire.");
  });

  it('should display an error when the email is invalid', () => {
    cy.get('#registerEmail').clear().type('invalid-email').blur();

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', "L'adresse e-mail n'est pas valide.");
  });

  it('should display an error when the email exceeds 50 characters', () => {
    const longEmail = `${'a'.repeat(40)}@example.com;`;

    cy.get('#registerEmail').clear().type(longEmail).blur();

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', '50 caractères');
  });

  it('should reject a username shorter than 3 characters', () => {
    cy.get('#registerUsername').clear().type('ab').blur();

    cy.get('app-button button').should('be.disabled');
  });

  it('should reject a username longer than 20 characters', () => {
    cy.get('#registerUsername').clear().type('thisusernameiswaytoolong').blur();

    cy.get('app-button button').should('be.disabled');
  });

  it('should display an error when the username is already taken', () => {
    cy.intercept('GET', '**/username-available*', {
      statusCode: 200,
      body: { available: false },
    }).as('usernameUnavailable');

    cy.get('#registerUsername').clear().type('existinguser');

    cy.wait('@usernameUnavailable');

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', 'déjà utilisé');

    cy.get('app-button button').should('be.disabled');
  });

  it('should display an error when username availability cannot be checked', () => {
    cy.intercept('GET', '**/username-available*', {
      statusCode: 500,
      body: { message: 'Server error' },
    }).as('usernameCheckError');

    cy.get('#registerUsername').clear().type('newusername');

    cy.wait('@usernameCheckError');

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', 'Impossible de vérifier la disponibilité');

    cy.get('app-button button').should('be.disabled');
  });

  it('should reject a password shorter than 8 characters', () => {
    cy.get('#registerPassword').clear().type('Ab1!').blur();

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', 'Minimum 8 caractères.');

    cy.get('app-button button').should('be.disabled');
  });

  it('should reject a password without an uppercase letter', () => {
    cy.get('#registerPassword').clear().type('password123!').blur();

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', 'Au moins une lettre majuscule.');

    cy.get('app-button button').should('be.disabled');
  });

  it('should reject a password without a lowercase letter', () => {
    cy.get('#registerPassword').clear().type('PASSWORD123!').blur();

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', 'Au moins une lettre minuscule.');

    cy.get('app-button button').should('be.disabled');
  });

  it('should reject a password without a number', () => {
    cy.get('#registerPassword').clear().type('Password!!!').blur();

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', 'Au moins un chiffre.');

    cy.get('app-button button').should('be.disabled');
  });

  it('should reject a password without a special character', () => {
    cy.get('#registerPassword').clear().type('Password123').blur();

    cy.get('mat-error')
      .should('be.visible')
      .and('contain.text', 'Au moins un caractère spécial.');

    cy.get('app-button button').should('be.disabled');
  });

  it('should update the profile successfully', () => {
    cy.intercept(
      {
        method: 'GET',
        pathname: '/api/v1/auth/username-available',
        query: { username: 'updateduser' },
      },
      {
        statusCode: 200,
        body: { available: true },
      },
    ).as('updatedUsernameAvailable');

    cy.intercept('PUT', '**/api/v1/users/me', {
      statusCode: 200,
      body: {
        ...currentUser,
        email: 'updated@example.com',
        username: 'updateduser',
      },
    }).as('updateProfile');

    fillValidForm();

    // Wait for the availability check for the updated username.
    cy.wait('@updatedUsernameAvailable');

    // Verify that the form is valid before submitting.
    cy.get('app-button button')
      .should('contain.text', 'Sauvegarder')
      .and('be.enabled')
      .click();

    // Verify the profile update request.
    cy.wait('@updateProfile').its('request.body').should('deep.equal', {
      email: 'updated@example.com',
      username: 'updateduser',
      password: 'Password123!',
    });
  });

  it('should display an error when profile update fails', () => {
    cy.intercept(
      {
        method: 'GET',
        pathname: '/api/v1/auth/username-available',
        query: { username: 'updateduser' },
      },
      {
        statusCode: 200,
        body: { available: true },
      },
    ).as('updatedUsernameAvailable');

    cy.intercept('PUT', '**/api/v1/users/me', {
      statusCode: 400,
      body: { message: 'Unable to update profile' },
    }).as('updateProfileError');

    fillValidForm();

    // Wait for the availability check for the updated username.
    cy.wait('@updatedUsernameAvailable');

    // Wait until the form becomes valid.
    cy.get('app-button button').should('be.enabled');

    // Submit the form.
    cy.get('app-button button').click();

    // Verify that the API rejects the update.
    cy.wait('@updateProfileError');

    // Verify the error message displayed to the user.
    cy.get('p.profile__error[role="alert"]')
      .should('be.visible')
      .and('contain.text', "Une erreur est survenue lors de l'inscription.");
  });

  it('should display an empty state when the user has no subscribed topics', () => {
    cy.contains('p.profile__empty', "Vous n'êtes abonné à aucun thème.").should(
      'be.visible',
    );
  });

  it('should display subscribed topics', () => {
    cy.intercept('GET', '**/api/v1/topics/subscribed*', {
      statusCode: 200,
      body: {
        content: [subscribedTopic],
        number: 0,
        size: 10,
        totalElements: 1,
        totalPages: 1,
        last: true,
      },
    }).as('getTopicsWithSubscription');

    cy.visit('/profile');

    cy.wait('@getTopicsWithSubscription');

    cy.get('app-topic-card').should('have.length', 1);
  });

  it('should display a loading spinner while topics are loading', () => {
    cy.intercept('GET', '**/api/v1/topics/subscribed*', {
      statusCode: 200,
      delay: 1000,
      body: emptyPage,
    }).as('slowSubscribedTopics');

    cy.visit('/profile');

    cy.get('mat-spinner').should('be.visible');

    cy.wait('@slowSubscribedTopics');
  });
});
