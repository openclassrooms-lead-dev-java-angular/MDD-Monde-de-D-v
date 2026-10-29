describe('Topic', () => {
  const topics = Array.from({ length: 10 }, (_, index) => ({
    id: index + 1,
    name: `Topic ${index + 1}`,
    slug: `topic-${index + 1}`,
    description: `Découvrez le topic ${index + 1}.`,
    subscribed: false,
  }));

  const emptyPage = {
    content: [],
    number: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    last: true,
  };

  const topicsPage = {
    content: topics,
    number: 0,
    size: 10,
    totalElements: 10,
    totalPages: 1,
    last: true,
  };

  beforeEach(() => {
    cy.login();

    cy.intercept('GET', '**/api/v1/topics?page=0&size=10', {
      statusCode: 200,
      body: topicsPage,
    }).as('getTopics');

    cy.visit('/topics');
  });

  it('should display the topic page', () => {
    cy.wait('@getTopics');

    cy.get('app-topic').should('exist');
    cy.get('app-topic-card').should('have.length', 10);
    cy.contains('Topic 1').should('be.visible');
    cy.contains('Topic 2').should('be.visible');
  });

  it('should display a loading message while topics are loading', () => {
    cy.intercept('GET', '**/api/v1/topics?page=0&size=10', {
      statusCode: 200,
      delay: 1000,
      body: topicsPage,
    }).as('slowGetTopics');

    cy.visit('/topics');

    cy.contains('.topic-list__loader', 'Chargement...').should('be.visible');

    cy.wait('@slowGetTopics');

    cy.get('.topic-list__loader').should('not.exist');
  });

  it('should display an empty state when no topics are available', () => {
    cy.intercept('GET', '**/api/v1/topics?page=0&size=10', {
      statusCode: 200,
      body: emptyPage,
    }).as('getEmptyTopics');

    cy.visit('/topics');

    cy.wait('@getEmptyTopics');

    cy.contains('.topic-list__empty', 'Aucun thème disponible.').should(
      'be.visible',
    );

    cy.get('app-topic-card').should('not.exist');
  });

  it('should display an error when topics cannot be loaded', () => {
    cy.intercept('GET', '**/api/v1/topics?page=0&size=10', {
      statusCode: 500,
      body: { message: 'Server error' },
    }).as('getTopicsError');

    cy.visit('/topics');

    cy.wait('@getTopicsError');

    cy.get('.topic-list__loader').should('not.exist');
    cy.get('app-topic-card').should('not.exist');
  });

  it('should subscribe to a topic successfully', () => {
    cy.intercept('POST', '**/api/v1/topics/**', {
      statusCode: 200,
      body: null,
    }).as('subscribeToTopic');

    cy.wait('@getTopics');

    cy.get('app-topic-card')
      .first()
      .within(() => {
        cy.contains('button', 'S’abonner').click();
      });

    cy.wait('@subscribeToTopic').then(({ request }) => {
      expect(request.method).to.equal('POST');
      expect(request.url).to.include('/api/v1/topics/');
    });

    cy.get('app-topic-card').first().should('contain.text', 'Topic 1');
  });

  it('should display an error when subscribing to a topic fails', () => {
    cy.intercept('POST', '**/api/v1/topics/**', {
      statusCode: 500,
      body: { message: 'Server error' },
    }).as('subscribeToTopicError');

    cy.wait('@getTopics');

    cy.get('app-topic-card')
      .first()
      .within(() => {
        cy.contains('button', 'S’abonner').click();
      });

    cy.wait('@subscribeToTopicError').then(({ request, response }) => {
      expect(request.method).to.equal('POST');
      expect(response?.statusCode).to.equal(500);
    });

    cy.get('.topic-list__empty').should('not.exist');
  });

  it('should load the next page of topics', () => {
    const nextTopics = Array.from({ length: 10 }, (_, index) => ({
      id: index + 11,
      name: `Topic ${index + 11}`,
      slug: `topic-${index + 11}`,
      description: `Découvrez le topic ${index + 11}.`,
      subscribed: false,
    }));

    const firstPageWithMoreTopics = {
      ...topicsPage,
      last: false,
      totalElements: 20,
      totalPages: 2,
    };

    const nextPage = {
      content: nextTopics,
      number: 1,
      size: 10,
      totalElements: 20,
      totalPages: 2,
      last: true,
    };

    cy.intercept('GET', '**/api/v1/topics?page=0&size=10', {
      statusCode: 200,
      body: firstPageWithMoreTopics,
    }).as('getFirstPageWithMoreTopics');

    cy.intercept('GET', '**/api/v1/topics?page=1&size=10', {
      statusCode: 200,
      body: nextPage,
    }).as('getNextPage');

    cy.visit('/topics');

    cy.wait('@getFirstPageWithMoreTopics');

    cy.get('app-topic-card').should('have.length', 10);

    // Scroll the page to trigger infinite scrolling.
    cy.scrollTo('bottom');

    cy.wait('@getNextPage');

    cy.get('app-topic-card').should('have.length', 20);
    cy.contains('Topic 11').should('be.visible');
    cy.contains('Topic 20').should('be.visible');
  });
});
