describe('Feed page', () => {
  const feedUrl = '**/api/v1/articles/feed*';

  const articles = [
    {
      publicId: 'premier-article',
      title: 'Premier article',
      content: 'Contenu du premier article',
      createdAt: '2026-10-20T10:00:00.000Z',
      updatedAt: '2026-10-20T10:00:00.000Z',
      username: 'Alice',
      topic: { publicId: 'topic-1', name: 'Angular' },
      comments: [],
    },
    {
      publicId: 'deuxieme-article',
      title: 'Deuxième article',
      content: 'Contenu du deuxième article',
      createdAt: '2026-10-21T10:00:00.000Z',
      updatedAt: '2026-10-21T10:00:00.000Z',
      username: 'Bob',
      topic: { publicId: 'topic-2', name: 'Java' },
      comments: [],
    },
  ];

  const createPage = (
    content: typeof articles,
    number: number,
    last: boolean,
  ) => ({
    content,
    number,
    size: 10,
    totalElements: content.length,
    totalPages: last ? number + 1 : number + 2,
    first: number === 0,
    last,
    empty: content.length === 0,
  });

  beforeEach(() => {
    cy.login();
  });

  it('should display the articles returned by the API', () => {
    cy.intercept('GET', feedUrl, {
      statusCode: 200,
      body: createPage(articles, 0, true),
    }).as('getArticles');

    cy.visit('/feed');
    cy.wait('@getArticles');

    cy.contains('app-article-card', 'Premier article').should('be.visible');
    cy.contains('app-article-card', 'Deuxième article').should('be.visible');
  });

  it('should display the create article button', () => {
    cy.intercept('GET', feedUrl, {
      statusCode: 200,
      body: createPage([], 0, true),
    }).as('getArticles');

    cy.visit('/feed');
    cy.wait('@getArticles');

    cy.contains('app-button', 'Ajouter un article').should('be.visible');
  });

  it('should navigate to the article creation page', () => {
    cy.intercept('GET', feedUrl, {
      statusCode: 200,
      body: createPage([], 0, true),
    }).as('getArticles');

    cy.visit('/feed');
    cy.wait('@getArticles');

    cy.contains('app-button', 'Ajouter un article').click();

    cy.url().should('include', '/create-article');
  });

  it('should display the default sort direction', () => {
    cy.intercept('GET', feedUrl, {
      statusCode: 200,
      body: createPage([], 0, true),
    }).as('getArticles');

    cy.visit('/feed');
    cy.wait('@getArticles');

    cy.get('.btn-sort')
      .should('have.attr', 'aria-label', 'Trier par date croissante');

    cy.get('.sort-icon').should('contain.text', '↓');
  });

  it('should display an empty state when no articles are available', () => {
    cy.intercept('GET', feedUrl, {
      statusCode: 200,
      body: createPage([], 0, true),
    }).as('getArticles');

    cy.visit('/feed');
    cy.wait('@getArticles');

    cy.contains('Aucun article disponible.').should('be.visible');
  });

  it('should load the next page when scrolling to the bottom', () => {
    cy.intercept('GET', '**/api/v1/articles/feed?page=0*', {
      statusCode: 200,
      body: createPage(articles, 0, false),
    }).as('getFirstPage');

    cy.intercept('GET', '**/api/v1/articles/feed?page=1*', {
      statusCode: 200,
      body: createPage(
        [
          {
            ...articles[0],
            publicId: 'troisieme-article',
            title: 'Troisième article',
          },
        ],
        1,
        true,
      ),
    }).as('getSecondPage');

    cy.visit('/feed');
    cy.wait('@getFirstPage');

    cy.get('.article-list__grid').then(($grid) => {
      const element = $grid[0];

      element.dispatchEvent(
        new Event('scroll', {
          bubbles: true,
        }),
      );
    });

    cy.window().then((win) => {
      win.dispatchEvent(
        new Event('scroll', {
          bubbles: true,
        }),
      );
    });

    cy.wait('@getSecondPage');
    cy.contains('app-article-card', 'Troisième article').should('be.visible');
  });

  it('should not load another page when all pages have been loaded', () => {
    cy.intercept('GET', '**/api/v1/articles/feed?page=0*', {
      statusCode: 200,
      body: createPage(articles, 0, true),
    }).as('getLastPage');

    cy.intercept('GET', '**/api/v1/articles/feed?page=1*').as('getUnexpectedPage');

    cy.visit('/feed');
    cy.wait('@getLastPage');

    cy.get('.article-list__grid').then(($grid) => {
      $grid[0].dispatchEvent(
        new Event('scroll', {
          bubbles: true,
        }),
      );
    });

    cy.window().then((win) => {
      win.dispatchEvent(
        new Event('scroll', {
          bubbles: true,
        }),
      );
    });

    cy.get('@getUnexpectedPage.all').should('have.length', 0);
  });

  it('should stop loading when the API returns an error', () => {
    cy.intercept('GET', feedUrl, {
      statusCode: 500,
      body: { message: 'Internal server error' },
    }).as('getArticlesError');

    cy.visit('/feed');
    cy.wait('@getArticlesError');

    cy.get('.btn-sort').should('not.be.disabled');
  });
});