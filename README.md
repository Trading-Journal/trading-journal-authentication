# Trading Journal Authentication

## Change Log

### 3.0.0
* Spring 3.1.0

### 2.0.0
* Change the /authentication/ endpoint to /auth/
  * 2.0.1
    * Fix CORS configuration
  * 2.0.2
    * Accept more METHODS for cors

### 1.0.0
* Fully functional authentication with JWT generation and interpretation made in this project code
  * 1.1.0
    * Fully functional authentication with JWT library
  * 1.1.1
     * Kubernetes deploy via CI with postman testing on Pull Request

## Pending for the future
* Manage user session
  * Can cancel a user session forcing a new login
  * Cache the session status somehow
    * Update this cache during login with time equal to jwt expiration
    * Evict this cache when delete user session
  * JWT lib will have a new version to validate JWT against some API
    * Use configuration for that
    * The API must receive the JWT and check if session is still valid
* * One way ssl or Two way ssl: https://dzone.com/articles/hakky54mutual-tls-1

## Swagger

[http://localhost:8080/swagger-ui/index.htm](http://localhost:8080/swagger-ui/index.html)
Or just [http://localhost:8080](http://localhost:8080)

## Running

### Locally

Set the active profile as local:

```bash
-Dspring.active.profiles=local
```

### Environment Variables

Default application properties used on deployed/container run require a set of Environment Variables:

* Generic
  * **PORT**: default is 8080
  * **ENVIRONMENT**: Environment name, mostly used for logging in logback file, default is DEFAULT
* Email
  * **EMAIL_HOST**: email host address 
  * **EMAIL_PASSWORD**: password to connect SMTP server
  * **EMAIL_USERNAME**: username to connect SMTP server
  * **EMAIL_PORT**: SMTP port
* Database
  * **DATASOURCE_URL**: datasource/server location url with database name
  * **DATASOURCE_USERNAME**: Username to connect database (read and write)
  * **DATASOURCE_PASSWORD**: Password of the username to connect database
* Reference for email links
  * **WEB_APP_URL**: Web app url where email links will be redirected to
  * **VERIFICATION_PATH**: Users verification page where email links will be redirected to, default is _auth/email-verified_
  * **CHANGE_PASSWORD_PAGE**: Change password page where email links will be redirected to, default is _auth/change-password_
* Properties using during application startup and first run
  * **ADMIN_EMAIL**: Email of application admin, this user will be created at the first run if there is no other ADMIN available
* JWT Properties
  * **JWT_PRIVATE_KEY**: Private key file used to sign access tokens
  * **JWT_PUBLIC_KEY**: public key file based on private key used to read access tokens
  * **JWT_ACCESS_TOKEN_EXPIRATION**: time to expire access token, default is 3600 seconds
  * **JWT_REFRESH_TOKEN_EXPIRATION**: time to expire refresh token, default is 86400 seconds
  * **JWT_ISSUER**: Access token issuer
  * **JWT_AUDIENCE**: Access token audience

### Container Dependencies

#### Postgres

```bash
docker run -d --name postgres -e POSTGRES_PASSWORD=trade-journal -e POSTGRES_USER=trade-journal -e POSTGRES_DB=trade-journal -p 5432:5432 postgres
```

### Keys Dependencies

```bash
# Generate a secret
openssl genrsa -out secret_key.pem 2048
# Transform the secret into a private key, to sign the jwt token
openssl pkcs8 -topk8 -inform PEM -outform PEM -in secret_key.pem -out private_key.pem -nocrypt
# Generate a public key from the secret to verify the jwt token
openssl rsa -in secret_key.pem -pubout -outform PEM -out public_key.pem
```

### Database Schema

```
CREATE USER trading_journal_user WITH ENCRYPTED PASSWORD '<PASSWORD>';
GRANT ALL PRIVILEGES ON DATABASE trade_journal TO trading_journal_user;

CREATE TABLE Tenancy (
  id SERIAL NOT NULL,
  name VARCHAR(254) NOT NULL,
  userLimit int NOT NULL DEFAULT 1,
  userUsage int NOT NULL DEFAULT 0,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE(name)
);

CREATE TABLE Users (
  id SERIAL NOT NULL,
  tenancyId int NULL,
  userName VARCHAR(45) NOT NULL,
  password VARCHAR(2000) NOT NULL,
  firstName VARCHAR(45) NOT NULL,
  lastName VARCHAR(45) NOT NULL,
  email VARCHAR(150) NOT NULL,
  enabled BOOLEAN NOT NULL,
  verified BOOLEAN NOT NULL,
  createdAt TIMESTAMP NOT NULL,
  newsletter BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id),
  CONSTRAINT tenancyIdFk FOREIGN KEY (tenancyId) REFERENCES Tenancy (id)
);

CREATE TABLE Authorities (
  id SERIAL NOT NULL,
  category VARCHAR(50) NOT NULL,
  name VARCHAR(45) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE(name)
);

CREATE TABLE UserAuthorities (
  id SERIAL NOT NULL,
  userId int NOT NULL,
  authorityId int NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT authorityIdFk FOREIGN KEY (authorityId) REFERENCES Authorities (id) ON DELETE CASCADE,
  CONSTRAINT userIdFk FOREIGN KEY (userId) REFERENCES Users (id) ON DELETE CASCADE
);

CREATE TABLE Verifications (
  id SERIAL NOT NULL,
  email VARCHAR(150) NOT NULL,
  type VARCHAR(45) NOT NULL,
  status VARCHAR(45) NOT NULL,
  hash VARCHAR(2000) NOT NULL,
  lastChange TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

GRANT ALL ON ALL TABLES IN SCHEMA public TO trading_journal_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO trading_journal_user;

INSERT INTO Authorities (category, name) VALUES ('COMMON_USER','ROLE_USER');
INSERT INTO Authorities (category, name) VALUES ('ADMINISTRATOR','ROLE_ADMIN');
INSERT INTO Authorities (category, name) VALUES ('ORGANISATION','TENANCY_ADMIN');
```

## Docker

### Build

```docker build -t allanweber/trading-journal-authentication:<VERSION> -f docker/Dockerfile .```

Tag your image to latest: ```docker tag allanweber/trading-journal-authentication:<VERSION> allanweber/trading-journal-authentication:latest``` 

Push image to registry: ```docker push allanweber/trading-journal-authentication:<VERSION>```

#### Docker Composer

Helpful for local testing
  
```docker-compose up```

```bash
docker run -p 8080:8080 --name trading-journal-authentication \
-e ADMIN_EMAIL= \
-e DATASOURCE_URL= \
-e DATASOURCE_PASSWORD= \
-e DATASOURCE_USERNAME= \
-e EMAIL_HOST= \
-e EMAIL_PORT= \
-e EMAIL_USERNAME= \
-e EMAIL_PASSWORD= \
-e JWT_ACCESS_TOKEN_EXPIRATION= \
-e JWT_REFRESH_TOKEN_EXPIRATION= \
-e JWT_AUDIENCE= \
-e JWT_ISSUER= \
-e JWT_PRIVATE_KEY= \
-e JWT_PUBLIC_KEY= \
-e WEB_APP_URL= \
allanweber/trading-journal-authentication:VERSION
```

## Configurations

### Start up application the first time
* **journal.authentication.admin-user.email**: email of the application admin, this will be used to generate a nre Admin user, and send the confirmations like user registration, change password etc.

### Verifications
In case there is need for new users confirm their emails, then enable the configuration property:

* **journal.authentication.verification.enabled** *e.g. true*
* **journal.authentication.hosts.front-end** *e.g. http://localhost:8080* to be able to proper redirect the user to the page of confirm registration or change password
* **journal.authentication.hosts.verification-page** *e.g. auth/email-verified* the web page user will be redirected to confirm the email
* **journal.authentication.hosts.change-password-page** *e.g. auth/change-password* the web page user will be redirected to change password