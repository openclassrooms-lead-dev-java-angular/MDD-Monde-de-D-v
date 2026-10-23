describe('Article creation form', () => {
  beforeEach(() => {
    cy.login();

    cy.intercept('GET', '**/api/v1/topics/options', {
      statusCode: 200,
      body: [
        { slug: 'angular', name: 'Angular' },
        { slug: 'java', name: 'Java' },
      ],
    }).as('getTopics');

    cy.intercept('GET', '**/api/v1/articles/available-slug/*', {
      statusCode: 200,
      body: { available: true },
    }).as('checkSlug');

    cy.visit('/create-article');
    cy.wait('@getTopics');
  });

  it('should display the article creation form', () => {
    cy.get('.article-form').should('be.visible');
    cy.contains('h1', 'Créer un nouvel article').should('be.visible');

    cy.get('#article-title').should('be.visible');
    cy.get('#article-slug').should('be.visible');
    cy.get('#article-content').should('be.visible');
    cy.get('mat-select').should('be.visible');

    cy.get('app-button button[type="submit"]')
      .should('be.visible')
      .and('contain.text', 'Créer');
  });

  it('should display the available topics', () => {
    cy.get('mat-select').click();

    cy.get('mat-option').should('have.length', 2);
    cy.contains('mat-option', 'Angular').should('be.visible');
    cy.contains('mat-option', 'Java').should('be.visible');
  });

  it('should navigate back to the feed when clicking the back link', () => {
    cy.get('a.article-form__back').should('have.attr', 'href', '/feed').click();

    cy.url().should('include', '/feed');
  });

  it('should display validation errors when submitting an empty form', () => {
    cy.get('app-button button[type="submit"]').click();

    cy.contains('Veuillez sélectionner un thème.').should('be.visible');
    cy.contains('Le titre est obligatoire.').should('be.visible');
    cy.contains('L’adresse est obligatoire.').should('be.visible');
    cy.contains('Le contenu est obligatoire.').should('be.visible');
  });

  it('should generate a slug from the title when the slug is empty', () => {
    cy.get('#article-title').type('Mon Premier Article');
    cy.get('#article-title').blur();

    cy.get('#article-slug').should('have.value', 'Mon-Premier-Article');
  });

  it('should display an error when the slug is already taken', () => {
    cy.intercept('GET', '**/api/v1/articles/available-slug/mon-article', {
      statusCode: 200,
      body: { available: false },
    }).as('takenSlug');

    cy.get('#article-slug').clear().type('mon-article');

    cy.wait('@takenSlug')
      .its('response.body')
      .should('deep.equal', { available: false });

    // Blur the field so that the validation state and error message are updated.
    cy.get('#article-title').click();

    cy.contains('mat-error', 'Cette adresse est déjà utilisée.').should(
      'be.visible',
    );
  });

  it('should create an article and navigate to its detail page', () => {
    cy.intercept('POST', '**/api/v1/articles', {
      statusCode: 201,
      body: {
        id: 1,
        title: 'Mon article de test',
        slug: 'mon-article-de-test',
        content: 'Contenu de test pour cet article.',
        topicSlug: 'angular',
        createdAt: '2026-01-01T10:00:00.000Z',
        updatedAt: '2026-01-01T10:00:00.000Z',
      },
    }).as('createArticle');

    cy.get('mat-select').click();
    cy.contains('mat-option', 'Angular').click();

    cy.get('#article-title').clear().type('Mon article de test');

    cy.get('#article-slug')
      .clear()
      .type('mon-article-de-test')
      .should('have.value', 'mon-article-de-test');

    cy.get('#article-content')
      .clear()
      .type('Contenu de test pour cet article.');

    cy.get('app-button button[type="submit"]')
      .should('be.visible')
      .and('not.be.disabled')
      .click();

    cy.wait('@createArticle').then(({ request }) => {
      expect(request.body).to.include({
        topicSlug: 'angular',
        title: 'Mon article de test',
        slug: 'mon-article-de-test',
        content: 'Contenu de test pour cet article.',
      });
    });

    cy.url().should('include', '/articles/mon-article-de-test');
  });

  it('should display an error when article creation fails', () => {
    cy.intercept('POST', '**/api/v1/articles', {
      statusCode: 500,
      body: {
        message: 'Erreur lors de la création de l’article.',
      },
    }).as('createArticleError');

    cy.get('mat-select').click();
    cy.contains('mat-option', 'Angular').click();

    cy.get('#article-title').type('Mon article de test');
    cy.get('#article-slug').type('mon-article-de-test');
    cy.get('#article-content').type('Contenu de test pour cet article.');

    cy.get('app-button button[type="submit"]').click();

    cy.wait('@createArticleError');

    cy.get('[role="alert"]')
      .should('be.visible')
      .and('contain.text', 'Erreur lors de la création de l’article.');
  });
});
