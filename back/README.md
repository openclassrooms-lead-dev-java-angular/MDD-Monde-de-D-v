# MDD Backend


## MDD CLI

The CLI allows administrative tasks to be executed without starting the web server.

### Prerequisites
- Java 25
- Maven
- A .env file at the project root
- A running and accessible database

Environment variables are automatically loaded from the .env file by the `scripts/run.sh` script.

### Usage

The general command is:

./scripts/run.sh <command> [options]

#### Database seeding

The seed command is used to generate application seed data.

- Seed users
````shell
./scripts/run.sh seed --users
````
- Seed all data
````shell
./scripts/run.sh seed --all
````
- Clear the database and seed users
````shell
./scripts/run.sh seed --clear --users
````
- Clear the database and seed all data
````shell
./scripts/run.sh seed --clear --all
````

Options
--users

Seeds demo users.

./scripts/run.sh seed --users
--all

Seeds all available demo data.

./scripts/run.sh seed --all
--clear

Clears existing data before running the seed.

./scripts/run.sh seed --clear --users

⚠️ Warning: This option is destructive. Existing data may be permanently deleted.

Examples

Initialize an empty database with demo users:

./scripts/run.sh seed --clear --users

Fully initialize the database:

./scripts/run.sh seed --clear --all

Add users without deleting existing data:

./scripts/run.sh seed --users
Command structure
mdd
└── seed
├── --users
├── --all
└── --clear

The CLI uses Picocli to parse commands and options.

Launcher script

The scripts/run.sh script:

loads environment variables from the .env file;
retrieves the arguments passed to the CLI;
starts MddCli through Maven;
passes the arguments to Picocli.

For example:

./scripts/run.sh seed --clear --users

is passed to the application as:

seed --clear --users



### RSA keys

Generate an RSA key pair for JWT signing and verification:

```shell
openssl genrsa -out src/main/resources/keys/private.key 2048
```

```shell
openssl rsa -pubout -in src/main/resources/keys/private.key -out src/main/resources/keys/public.key
```

⚠️ The generated keys must never be committed to the repository.

