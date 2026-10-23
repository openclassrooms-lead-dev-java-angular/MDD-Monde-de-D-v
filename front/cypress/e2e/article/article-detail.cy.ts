import '../../support/commands';

describe('Article detail', () => {
  const articleSlug = 'decouvrir-angular';

  const article = {
    id: 1,
    slug: articleSlug,
    title: 'Découvrir Angular',
    description: 'Un article pour découvrir Angular.',
    content: '<p>Bienvenue dans cet article sur Angular.</p>',
    createdAt: '2026-01-15T10:00:00.000Z',
    updatedAt: '2026-01-15T10:00:00.000Z',
    username: 'testuser123',
    topic: {
      id: 1,
      name: 'Développement web',
      slug: 'developpement-web',
    },
    comments: [],
  };

  const comment = {
    id: 1,
    content: 'Merci pour cet article !',
    createdAt: '2026-01-16T10:00:00.000Z',
    updatedAt: '2026-01-16T10:00:00.000Z',
    username: 'testuser123',
  };

  const articlesFeedPage = {
    content: [article],
    number: 0,
    size: 10,
    totalElements: 1,
    totalPages: 1,
    last: true,
  };

  beforeEach(() => {
    cy.login();

    // Stub the feed request that may be triggered after login.
    cy.intercept(
      {
        method: 'GET',
        pathname: '/api/v1/articles/feed',
        query: {
          page: '0',
          size: '10',
          sort: 'createdAt,desc',
        },
      },
      {
        statusCode: 200,
        body: articlesFeedPage,
      },
    ).as('getArticlesFeed');

    cy.intercept('GET', `**/api/v1/articles/${articleSlug}`, {
      statusCode: 200,
      body: article,
    }).as('getArticle');
  });

  it('should display the article details', () => {
    cy.visit(`/articles/${articleSlug}`);

    cy.wait('@getArticle');

    cy.get('.article-detail').should('exist');
    cy.get('.article__title h1')
      .should('be.visible')
      .and('contain.text', 'Découvrir Angular');

    cy.get('.article__content')
      .should('be.visible')
      .and('contain.text', 'Bienvenue dans cet article sur Angular.');

    cy.get('.article__meta').should('contain.text', 'testuser123');
    cy.get('.article__meta').should('contain.text', 'Développement web');
    cy.get('.comments h2').should('contain.text', 'Commentaires');
  });

  it('should display a loading message while the article is loading', () => {
    cy.intercept('GET', `**/api/v1/articles/${articleSlug}`, {
      statusCode: 200,
      delay: 1000,
      body: article,
    }).as('slowGetArticle');

    cy.visit(`/articles/${articleSlug}`);

    cy.contains(
      '.article-detail__loading',
      "Chargement de l'article...",
    ).should('be.visible');

    cy.wait('@slowGetArticle');

    cy.get('.article-detail__loading').should('not.exist');
    cy.get('.article__title h1').should('contain.text', 'Découvrir Angular');
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
      });

    cy.get('.article').should('not.exist');
  });

  it('should display an empty state when the article request fails', () => {
    cy.intercept('GET', `**/api/v1/articles/${articleSlug}`, {
      statusCode: 500,
      body: { message: 'Server error' },
    }).as('getArticleError');

    cy.visit(`/articles/${articleSlug}`);

    cy.wait('@getArticleError');

    cy.get('.article-detail__loading').should('not.exist');
    cy.get('.article-detail__empty').should('be.visible');
    cy.contains('h1', 'Article introuvable').should('be.visible');
  });

  it('should display an empty state when the article has no comments', () => {
    cy.visit(`/articles/${articleSlug}`);

    cy.wait('@getArticle');

    cy.contains(
      '.comments__empty',
      "Il n'y a pas de commentaire, soyez le premier à en écrire un.",
    ).should('be.visible');

    cy.get('app-comment').should('not.exist');
  });

  it('should display the existing comments', () => {
    const articleWithComments = {
      ...article,
      comments: [
        comment,
        {
          ...comment,
          id: 2,
          content: 'Très intéressant !',
          username: 'anotheruser',
        },
      ],
    };

    cy.intercept('GET', `**/api/v1/articles/${articleSlug}`, {
      statusCode: 200,
      body: articleWithComments,
    }).as('getArticleWithComments');

    cy.visit(`/articles/${articleSlug}`);

    cy.wait('@getArticleWithComments');

    cy.get('app-comment').should('have.length', 2);
    cy.get('.comments__empty').should('not.exist');
  });

  it('should disable the submit button when the comment is empty', () => {
    cy.visit(`/articles/${articleSlug}`);

    cy.wait('@getArticle');

    cy.get('#comment-content').should('have.value', '');

    cy.get('button[aria-label="Envoyer le commentaire"]').should('be.disabled');
  });

  it('should submit a comment successfully', () => {
  const commentContent = 'Merci pour cet article !';

  const newComment = {
    id: 2,
    content: commentContent,
    createdAt: '2026-01-16T10:00:00.000Z',
    updatedAt: '2026-01-16T10:00:00.000Z',
    username: 'testuser123',
  };

  cy.intercept('GET', `**/api/v1/articles/${articleSlug}`, {
    statusCode: 200,
    body: article,
  }).as('getArticle');

  cy.intercept(
    {
      method: 'POST',
      pathname: `/api/v1/articles/${articleSlug}/comments`,
    },
    {
      statusCode: 201,
      body: newComment,
    },
  ).as('postComment');

  cy.visit(`/articles/${articleSlug}`);
  cy.wait('@getArticle');

  cy.get('#comment-content').type(commentContent);

  cy.get('button[aria-label="Envoyer le commentaire"]')
    .should('be.enabled')
    .click();

  cy.wait('@postComment').then(({ request, response }) => {
    expect(request.body).to.deep.equal({ content: commentContent });
    expect(response?.statusCode).to.equal(201);
  });

  cy.get('app-comment')
    .should('have.length', 1)
    .and('contain', commentContent);

  cy.get('#comment-content').should('have.value', '');
});

  it('should display the submitted comment in the comments list', () => {
    const newComment = {
      id: 2,
      content: 'Merci pour cet article !',
      createdAt: '2026-01-16T10:00:00.000Z',
      updatedAt: '2026-01-16T10:00:00.000Z',
      username: 'testuser123',
    };

    cy.intercept('GET', `**/api/v1/articles/${articleSlug}`, {
      statusCode: 200,
      body: article,
    }).as('getArticle');

    cy.intercept('POST', `**/api/v1/articles/${articleSlug}/comments`, {
      statusCode: 201,
      body: newComment,
    }).as('postComment');

    cy.visit(`/articles/${articleSlug}`);

    cy.wait('@getArticle');

    cy.get('#comment-content')
      .should('be.visible')
      .type('Merci pour cet article !');

    cy.get('button[aria-label="Envoyer le commentaire"]')
      .should('be.enabled')
      .click();

    cy.wait('@postComment').then(({ request, response }) => {
      expect(request.body).to.deep.equal({
        content: 'Merci pour cet article !',
      });

      expect(response?.statusCode).to.equal(201);
    });

    cy.get('app-comment').should('have.length', 1);
    cy.get('app-comment').should('contain', 'Merci pour cet article !');

    cy.get('#comment-content').should('have.value', '');
  });

  it('should return to the feed page when clicking the back button', () => {
    cy.intercept('GET', `**/api/v1/articles/${articleSlug}`, {
      statusCode: 200,
      body: article,
    }).as('getArticleForBackNavigation');

    // Open the feed page first.
    cy.visit('/feed');

    // Navigate to the article page.
    cy.visit(`/articles/${articleSlug}`);
    cy.wait('@getArticleForBackNavigation');

    cy.get('button.article__back-button')
      .should('be.visible')
      .and('have.attr', 'aria-label', "Retour à l'accueil")
      .click();

    cy.location('pathname').should('eq', '/feed');
  });
});
