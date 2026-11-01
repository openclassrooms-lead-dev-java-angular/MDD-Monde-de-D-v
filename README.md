# MDD — Monde de Dév

## 1. Project Overview

**MDD (Monde de Dév)** is a web application designed for a community of developers. It allows users to discover development-related topics, share articles, and interact with the community.

The project follows a full-stack architecture composed of two applications:

- `front/`: Angular frontend application.
- `back/`: Java and Spring Boot backend application.

The frontend communicates with the backend to access the application's features.

## 2. Project Architecture

```text
MDD-Monde-de-D-v/
├── front/
│   └── Angular application
├── back/
│   └── Java / Spring Boot application
└── README.md
```

## 3. Technologies

| Component | Technology |
|---|---|
| Frontend | Angular |
| Backend | Java |
| Backend framework | Spring Boot |
| Frontend unit testing | Jasmine / Karma |
| End-to-end testing | Cypress |
| Backend testing | JUnit |
| Backend code coverage | JaCoCo |

## 4. Prerequisites

The exact versions of the tools should be checked in the project's configuration files.

- Node.js and npm for the frontend.
- Java for the backend.
- OpenSSL to generate the RSA keys used for JWT authentication.
- The build tool configured for the backend.
- A database environment matching the backend configuration.

## 5. Frontend Installation and Configuration

### 5.1. Install dependencies

Navigate to the frontend directory:

```bash
cd front
```

Install the dependencies:

```bash
npm install
```

### 5.2. Run the application in development mode

Start the Angular development server:

```bash
npx ng serve
```

The application is available at:

```text
http://localhost:4200/
```

The development server automatically reloads the application when source files are modified.

### 5.3. Build the frontend

To generate a production build:

```bash
npx ng build
```

The generated files are placed in the `dist/` directory.

## 6. Backend Installation and Configuration

### 6.1. Environment variables

From the backend directory, create the `.env` file using the `.env.dev` template:

```bash
cp .env.dev .env
```

Open the newly created `.env` file and fill in all environment variables with values matching your local environment.

The values must be adapted to your configuration, particularly for any database connection and security settings used by the application.

**Important:** Do not commit the `.env` file if it contains sensitive information.

### 6.2. RSA key generation

The backend uses an RSA key pair to sign and verify JWT tokens.

From the backend root directory, generate the private key:

```bash
openssl genrsa -out src/main/resources/keys/private.key 2048
```

Then generate the public key from the private key:

```bash
openssl rsa -pubout \
  -in src/main/resources/keys/private.key \
  -out src/main/resources/keys/public.key
```

The following files will be generated:

| File | Purpose |
|---|---|
| `src/main/resources/keys/private.key` | Private key used to sign JWTs |
| `src/main/resources/keys/public.key` | Public key used to verify JWTs |

<escape>⚠️</escape> **Security warning: Generated RSA keys must never be committed to the repository.** They must remain specific to each environment. The private key must be kept secure and must never be shared publicly.

Make sure the following files are excluded from Git tracking:

```gitignore
.env
src/main/resources/keys/private.key
src/main/resources/keys/public.key
```

### 6.3. Start the backend

Once the environment variables have been configured and the RSA keys have been generated, the backend can be started using the commands corresponding to its build system.

The exact build and startup commands must be checked in the configuration files located in the `back/` directory.

## 7. Frontend and Backend Communication

The frontend communicates with the API exposed by the backend.

For a local environment, check the following:

- The API URL configured in the frontend matches the backend address.
- The frontend origin is allowed by the backend CORS configuration.
- The backend security rules allow the requests required by the application.

Differences between origins such as `localhost` and `127.0.0.1`, or between the ports being used, may cause CORS requests to be rejected.

## 8. Testing

### 8.1. Frontend — Unit tests

Frontend unit tests are implemented using Jasmine and Karma.

Reported code coverage results:

| Metric | Coverage |
|---|---:|
| Statements | 99.08% |
| Branches | 96.11% |
| Functions | 100% |
| Lines | 99.26% |

### 8.2. Frontend — End-to-end tests

End-to-end tests are implemented using Cypress.

Reported code coverage results:

| Metric | Coverage |
|---|---:|
| Statements | 90.40% |
| Branches | 83.77% |
| Functions | 88.71% |
| Lines | 90.45% |

### 8.3. Backend — Unit tests

Backend tests are implemented using JUnit. JaCoCo is used to measure code coverage.

Reported results:

| Metric | Result |
|---|---:|
| Instructions | 91% |
| Branches | 91% |
| Covered lines | 617 / 675 |
| Covered methods | 150 / 165 |
| Covered classes | 44 / 46 |

The exact test execution commands depend on the backend build configuration.

## 9. Frontend Performance Audit

A Lighthouse audit was performed on the compiled frontend application.

Results:

| Metric | Score |
|---|---:|
| Performance | 59 |
| Accessibility | 100 |
| Best Practices | 96 |
| SEO | 91 |

The audit identified potential areas for improvement, particularly JavaScript loading and resource usage, caching, and image delivery.

These results concern the frontend and do not represent a backend performance measurement.

## 10. GitHub Repository

The project's source code is available at:

https://github.com/openclassrooms-lead-dev-java-angular/MDD-Monde-de-D-v
