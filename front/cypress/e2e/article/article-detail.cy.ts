import '../../support/commands';

describe('Article detail', () => {
  const articlePublicId = 'article-123';

  const article = {
    publicId: articlePublicId,
    title: 'Découvrir Angular',
    content: '<p>Bienvenue dans cet article sur Angular.</p>',
    createdAt: '2026-01-15T10:00:00.000Z',
    updatedAt: '2026-01-15T10:00:00.000Z',
    username: 'testuser123',
    topic: {
      publicId: 'topic-123',
      name: 'Développement web',
    },
    comments: [],
  };

  const comment = {
    publicId: 'comment-123',
    content: 'Merci pour cet article !',
    createdAt: '2026-01-16T10:00:00.000Z',
    updatedAt: '2026-01-16T10:00:00.000Z',
    username: 'testuser123',
  };

  const articleUrl = `/articles/${articlePublicId}`;

  function interceptArticle(
    responseBody: typeof article = article,
  ): void {
    cy.intercept('GET', '**/api/v1/articles/article-123', {
      statusCode: 200,
      body: responseBody,
    }).as('getArticle');
  }

  beforeEach(() => {
    cy.login();
    interceptArticle();
  });

  it('should display the article details', () => {
    cy.visit(articleUrl);

    cy.wait('@getArticle');

    cy.get('.article-detail').should('be.visible');

    cy.get('.article__title h1')
      .should('be.visible')
      .and('contain.text', 'Découvrir Angular');

    cy.get('.article__content')
      .should('be.visible')
      .and('contain.text', 'Bienvenue dans cet article sur Angular.');

    cy.get('.article__meta')
      .should('contain.text', 'testuser123')
      .and('contain.text', 'Développement web');

    cy.get('.comments h2')
      .should('be.visible')
      .and('contain.text', 'Commentaires');
  });

  it('should display a loading message while the article is loading', () => {
    cy.intercept('GET', '**/api/v1/articles/article-123', {
      statusCode: 200,
      delay: 1000,
      body: article,
    }).as('slowGetArticle');

    cy.visit(articleUrl);

    cy.get('.article-detail__loading')
      .should('be.visible')
      .and('contain.text', "Chargement de l'article...");

    cy.wait('@slowGetArticle');

    cy.get('.article-detail__loading').should('not.exist');

    cy.get('.article__title h1')
      .should('contain.text', 'Découvrir Angular');
  });

  it('should display an empty state when the article does not exist', () => {
    cy.intercept('GET', '**/api/v1/articles/article-introuvable', {
      statusCode: 404,
      body: { message: 'Article not found' },
    }).as('getMissingArticle');

    cy.visit('/articles/article-introuvable');

    cy.wait('@getMissingArticle');

    cy.get('.article-detail__empty')
      .should('be.visible')
      .within(() => {
        cy.contains('h1', 'Article introuvable').should('be.visible');

        cy.contains(
          'p',
          "L'article que vous recherchez n'existe pas ou n'est plus disponible.",
        ).should('be.visible');

        cy.get('a.button')
          .should('be.visible')
          .and('contain.text', 'Retour aux articles');
      });

    cy.get('.article').should('not.exist');
  });

  it('should display the empty state when the article request fails', () => {
    cy.intercept('GET', '**/api/v1/articles/article-123', {
      statusCode: 500,
      body: { message: 'Server error' },
    }).as('getArticleError');

    cy.visit(articleUrl);

    cy.wait('@getArticleError');

    cy.get('.article-detail__loading').should('not.exist');
    cy.get('.article-detail__empty').should('be.visible');

    cy.contains('h1', 'Article introuvable').should('be.visible');
  });

  it('should display an empty state when the article has no comments', () => {
    cy.visit(articleUrl);

    cy.wait('@getArticle');

    cy.get('.comments__empty')
      .should('be.visible')
      .and(
        'contain.text',
        "Il n'y a pas de commentaire, soyez le premier à en écrire un.",
      );

    cy.get('app-comment').should('not.exist');
  });

  it('should display existing comments', () => {
    const articleWithComments = {
      ...article,
      comments: [
        comment,
        {
          ...comment,
          publicId: 'comment-456',
          content: 'Très intéressant !',
          username: 'anotheruser',
        },
      ],
    };

    cy.intercept('GET', '**/api/v1/articles/article-123', {
      statusCode: 200,
      body: articleWithComments,
    }).as('getArticleWithComments');

    cy.visit(articleUrl);

    cy.wait('@getArticleWithComments');

    cy.get('app-comment').should('have.length', 2);
    cy.get('.comments__empty').should('not.exist');

    cy.get('app-comment').eq(0).should('contain.text', 'Merci pour cet article !');
    cy.get('app-comment').eq(1).should('contain.text', 'Très intéressant !');
  });

  it('should disable the submit button when the comment is empty', () => {
    cy.visit(articleUrl);

    cy.wait('@getArticle');

    cy.get('#comment-content').should('have.value', '');

    cy.get('button[aria-label="Envoyer le commentaire"]')
      .should('be.disabled');
  });

  it('should enable the submit button when the comment contains text', () => {
    cy.visit(articleUrl);

    cy.wait('@getArticle');

    cy.get('#comment-content').type('Mon nouveau commentaire');

    cy.get('button[aria-label="Envoyer le commentaire"]')
      .should('be.enabled');
  });

  it('should submit a comment successfully and update the list', () => {
    const commentContent = 'Merci pour cet article !';

    const newComment = {
      ...comment,
      publicId: 'comment-new',
      content: commentContent,
    };

    cy.intercept(
      'POST',
      '**/api/v1/articles/article-123/comments',
      {
        statusCode: 201,
        body: newComment,
      },
    ).as('postComment');

    cy.visit(articleUrl);

    cy.wait('@getArticle');

    cy.get('#comment-content').type(commentContent);

    cy.get('button[aria-label="Envoyer le commentaire"]')
      .should('be.enabled')
      .click();

    cy.wait('@postComment').then(({ request, response }) => {
      expect(request.body).to.deep.equal({
        content: commentContent,
      });

      expect(response?.statusCode).to.equal(201);
    });

    cy.get('app-comment')
      .should('have.length', 1)
      .and('contain.text', commentContent);

    cy.get('#comment-content').should('have.value', '');
    cy.get('.comments__empty').should('not.exist');
  });

  it('should keep the comment form usable when submission fails', () => {
    cy.intercept(
      'POST',
      '**/api/v1/articles/article-123/comments',
      {
        statusCode: 500,
        body: { message: 'Server error' },
      },
    ).as('postCommentError');

    cy.visit(articleUrl);

    cy.wait('@getArticle');

    cy.get('#comment-content').type('Mon commentaire');

    cy.get('button[aria-label="Envoyer le commentaire"]')
      .should('be.enabled')
      .click();

    cy.wait('@postCommentError');

    cy.get('#comment-content').should('have.value', 'Mon commentaire');

    cy.get('button[aria-label="Envoyer le commentaire"]')
      .should('be.enabled');

    cy.get('app-comment').should('not.exist');
  });

  it('should navigate back when clicking the back button', () => {
    // Visit the feed first so that browser history contains /feed.
    cy.visit('/feed');

    cy.intercept('GET', '**/api/v1/articles/article-123', {
      statusCode: 200,
      body: article,
    }).as('getArticleForBackNavigation');

    cy.visit(articleUrl);

    cy.wait('@getArticleForBackNavigation');

    cy.get('button.article__back-button')
      .should('be.visible')
      .and('have.attr', 'aria-label', "Retour à l'accueil")
      .click();

    cy.location('pathname').should('eq', '/feed');
  });
});