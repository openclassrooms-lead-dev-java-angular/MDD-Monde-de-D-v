describe('Home', () => {
  beforeEach(() => {
    cy.visit('/');
  });

  it('should display the home page', () => {
    cy.get('app-home').should('exist');
    cy.get('.home').should('be.visible');
  });

  it('should display the logo', () => {
    cy.get('img.home__logo')
      .should('be.visible')
      .and('have.attr', 'src', '/assets/logo_p6.png')
      .and('have.attr', 'alt', 'logo');
  });

  it('should display the login button', () => {
    cy.get('.home__buttons app-button')
      .contains('Se connecter')
      .should('be.visible');
  });

  it('should display the registration button', () => {
    cy.get('.home__buttons app-button')
      .contains("S'inscrire")
      .should('be.visible');
  });

  it('should display both buttons in the buttons container', () => {
    cy.get('.home__buttons app-button').should('have.length', 2);

    cy.get('.home__buttons').should('be.visible');
  });

  it('should navigate to the login page when clicking the login button', () => {
    cy.get('.home__buttons app-button').contains('Se connecter').click();

    cy.url().should('include', '/login');
  });

  it('should navigate to the registration page when clicking the registration button', () => {
    cy.get('.home__buttons app-button').contains("S'inscrire").click();

    cy.url().should('include', '/register');
  });
});
