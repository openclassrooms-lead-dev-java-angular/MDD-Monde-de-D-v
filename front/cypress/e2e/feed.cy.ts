describe('Feed des articles', () => {
  const feedUrl = '**/articles/feed*';

  const articles = [
    {
      id: 1,
      title: 'Premier article',
      slug: 'premier-article',
      description: 'Description du premier article',
      content: 'Contenu du premier article',
      author: 'Alice',
      createdAt: '2026-10-20T10:00:00',
    },
    {
      id: 2,
      title: 'Deuxième article',
      slug: 'deuxieme-article',
      description: 'Description du deuxième article',
      content: 'Contenu du deuxième article',
      author: 'Bob',
      createdAt: '2026-10-21T10:00:00',
    },
  ];

  const pageResponse = (content: typeof articles, number = 0, last = true) => ({
    content,
    number,
    size: 10,
    totalElements: content.length,
    totalPages: 1,
    first: number === 0,
    last,
    empty: content.length === 0,
  });

  beforeEach(() => {
    cy.intercept('GET', feedUrl, {
      statusCode: 200,
      body: pageResponse(articles),
    }).as('getArticles');

    cy.visit('/feed');
    cy.wait('@getArticles');
  });

  it('devrait afficher la liste des articles', () => {
    cy.get('.article-list').should('be.visible');
    cy.get('.article-list__grid').should('be.visible');

    // À adapter si le composant ArticleCard utilise d'autres sélecteurs.
    cy.get('app-article-card').should('have.length', 2);
    cy.contains('app-article-card', 'Premier article').should('exist');
    cy.contains('app-article-card', 'Deuxième article').should('exist');
  });

  it('devrait afficher le bouton pour ajouter un article', () => {
    cy.get('.btn-create').should('be.visible');
    cy.contains('app-button', 'Ajouter un article').should('be.visible');
  });

  it('devrait naviguer vers la page de création', () => {
    cy.get('.btn-create').click();
    cy.url().should('include', '/create-article');
  });

  it('devrait afficher le bouton de tri avec le bon libellé initial', () => {
    cy.get('.btn-sort')
      .should('be.visible')
      .and('have.attr', 'aria-label', 'Trier par date croissante');

    cy.get('.sort-icon').should('contain.text', '↓');
  });

  it('devrait afficher le message si aucun article n’est disponible', () => {
    cy.intercept('GET', feedUrl, {
      statusCode: 200,
      body: pageResponse([]),
    }).as('getEmptyFeed');

    cy.visit('/feed');
    cy.wait('@getEmptyFeed');

    cy.get('.empty-state')
      .should('be.visible')
      .and('contain.text', 'Aucun article disponible.');

    cy.get('app-article-card').should('not.exist');
  });

  it('devrait charger la page suivante lors du scroll infini', () => {
    // On utilise une réponse différente pour chaque page.
    cy.intercept('GET', '**/articles/feed*', (req) => {
      const url = new URL(req.url);
      const page = Number(url.searchParams.get('page') ?? 0);

      if (page === 0) {
        req.alias = 'firstPage';
        req.reply({
          statusCode: 200,
          body: {
            ...pageResponse(articles, 0, false),
            totalElements: 12,
            totalPages: 2,
          },
        });
      } else {
        req.alias = 'secondPage';
        req.reply({
          statusCode: 200,
          body: pageResponse(
            [
              {
                ...articles[0],
                id: 3,
                title: 'Article de la deuxième page',
                slug: 'article-deuxieme-page',
              },
            ],
            1,
            true,
          ),
        });
      }
    }).as('feedPages');

    cy.visit('/feed');
    cy.wait('@firstPage');

    // Déclenche l'événement de scroll sur le conteneur.
    cy.get('.article-list__grid').scrollTo('bottom');
    cy.wait('@secondPage');

    cy.contains('app-article-card', 'Article de la deuxième page').should(
      'exist',
    );
  });

  it('devrait gérer une erreur du serveur sans rester bloqué en chargement', () => {
    cy.intercept('GET', feedUrl, {
      statusCode: 500,
      body: { message: 'Erreur serveur' },
    }).as('getArticlesError');

    cy.visit('/feed');
    cy.wait('@getArticlesError');

    cy.get('.article-list').should('be.visible');
    cy.get('.btn-sort').should('not.be.disabled');
  });
});
