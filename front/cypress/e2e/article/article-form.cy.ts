describe('Article creation form', () => {
  beforeEach(() => {
    cy.login();

    cy.intercept('GET', '**/api/v1/topics/options', {
      statusCode: 200,
      body: [
        { publicId: 'topic-1', name: 'Angular' },
        { publicId: 'topic-2', name: 'Java' },
      ],
    }).as('getTopics');

    cy.visit('/create-article');
    cy.wait('@getTopics');
  });

  it('should display the article creation form', () => {
    cy.get('.article-form').should('be.visible');

    cy.contains('h1', 'Créer un nouvel article').should('be.visible');

    cy.get('#article-title').should('be.visible');
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

  it('should select a topic', () => {
    cy.get('mat-select').click();
    cy.contains('mat-option', 'Angular').click();

    cy.get('mat-select').should('contain.text', 'Angular');
  });

  it('should navigate back to the feed when clicking the back link', () => {
    cy.get('a.article-form__back').should('have.attr', 'href', '/feed').click();

    cy.url().should('include', '/feed');
  });

  it('should display validation errors when submitting an empty form', () => {
    cy.get('app-button button[type="submit"]').click();

    cy.contains('mat-error', 'Veuillez sélectionner un thème.').should(
      'be.visible',
    );

    cy.contains('mat-error', 'Le titre est obligatoire.').should('be.visible');

    cy.contains('mat-error', 'Le contenu est obligatoire.').should(
      'be.visible',
    );
  });

  it('should display an error when the title exceeds 150 characters', () => {
    cy.get('#article-title').type('a'.repeat(151));
    cy.get('#article-title').blur();

    cy.contains(
      'mat-error',
      'Le titre ne doit pas dépasser 150 caractères.',
    ).should('be.visible');
  });

  it('should display an error when the content exceeds 10000 characters', () => {
    cy.get('#article-content').type('a'.repeat(10001), {
      delay: 0,
      parseSpecialCharSequences: false,
    });

    cy.get('#article-content').blur();

    cy.contains(
      'mat-error',
      'Le contenu ne doit pas dépasser 10 000 caractères.',
    ).should('be.visible');
  });

  it('should create an article and navigate to its detail page', () => {
    cy.intercept('POST', '**/api/v1/articles', {
      statusCode: 201,
      body: {
        publicId: 'article-123',
        title: 'Mon article de test',
        content: 'Contenu de test pour cet article.',
        topicPublicId: 'topic-1',
      },
    }).as('createArticle');

    cy.get('mat-select').click();
    cy.contains('mat-option', 'Angular').click();

    cy.get('#article-title').type('Mon article de test');

    cy.get('#article-content').type('Contenu de test pour cet article.');

    cy.get('app-button button[type="submit"]')
      .should('be.visible')
      .and('not.be.disabled')
      .click();

    cy.wait('@createArticle').then(({ request }) => {
      expect(request.body).to.deep.equal({
        topicPublicId: 'topic-1',
        title: 'Mon article de test',
        content: 'Contenu de test pour cet article.',
      });
    });

    cy.url().should('include', '/articles/article-123');
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

    cy.get('#article-content').type('Contenu de test pour cet article.');

    cy.get('app-button button[type="submit"]')
      .should('not.be.disabled')
      .click();

    cy.wait('@createArticleError');

    cy.get('[role="alert"]')
      .should('be.visible')
      .and('contain.text', 'Erreur lors de la création de l’article.');
  });
});
