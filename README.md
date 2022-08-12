# Trading Journal Authentication

## Pending

* Set and document environment variables and properties
* Test Container/Kubernetes deploy with adding keys files
* Postman Test run on pipeline
* One way ssl or Two way ssl: https://dzone.com/articles/hakky54mutual-tls-1
* Lib for token validation to be used in other projects
  * ApplicationUser Interface, ContextUser and Token Provider, JwtTokenAuthenticationFilter and All JWT package must be here
  * Consider:
    * Token validation
    * Token reader
    * With Private key/certificate
    * etc
* Use token generated here in another project to validate flow and Lib (above)
* Create version 1.0.0
* Create TAG with current code for reuse in other projects

## Pending for the future
* Manage user session
  * Can cancel a user session forcing a new login
  * Cache the session status somehow
    * Update this cache during login with time equal to jwt expiration
    * Evict this cache when delete user session
  * JWT lib will have a new version to validate JWT against some API
    * Use configuration for that
    * The API must receive the JWT and check if session is still valid

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
* Email
  * **EMAIL_HOST**: email host address 
  * **EMAIL_PASSWORD**: password to connect SMTP server
  * **EMAIL_USERNAME**: username to connect SMTP server
  * **EMAIL_PORT**: SMTP port
* Database
  * **DATASOURCE_URL**: datasource/server location url with database name
  * **DATASOURCE_USERNAME**: Username to connect database (read and write)
  * **DATASOURCE_PASSWORD**: Password of the username to connect database
  * **DATASOURCE_DRIVER**: Driver of the database, default is _com.mysql.cj.jdbc.Driver_
* Reference for email links
  * **WEB_APP_URL**: Web app url where email links will be redirected to
  * **VERIFICATION_PATH**: Users verification page where email links will be redirected to, default is _auth/email-verified_
  * **CHANGE_PASSWORD_PAGE**: Change password page where email links will be redirected to, default is _auth/change-password_
* Properties using during application startup and first run
  * **ADMIN_EMAIL**: Email of application admin, this user will be created at the first run if there is no other ADMIN available
* JWT Properties
  * **JWT_PRIVATE_KEY**: Private key used to sign access tokens
  * **JWT_PUBLIC_KEY**: public based on private key used to read access tokens
  * **JWT_ACCESS_TOKEN_EXPIRATION**: time to expire access token, default is 3600 seconds
  * **JWT_REFRESH_TOKEN_EXPIRATION**: time to expire refresh token, default is 86400 seconds
  * **JWT_ISSUER**: Access token issuer
  * **JWT_AUDIENCE**: Access token audience

### Container Dependencies

#### MySql

```bash
docker run -d -e MYSQL_USER=trade-journal -e MYSQL_PASSWORD=trade-journal -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=trade-journal -p 3306:3306 mysql:latest
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
CREATE TABLE `Tenancy` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(254) NOT NULL,
  `userLimit` int NOT NULL DEFAULT 1,
  `userUsage` int NOT NULL DEFAULT 0,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
);

CREATE TABLE `Users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenancyId` int NULL,
  `userName` varchar(45) NOT NULL,
  `password` varchar(2000) NOT NULL,
  `firstName` varchar(45) NOT NULL,
  `lastName` varchar(45) NOT NULL,
  `email` varchar(150) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  `verified` tinyint(1) NOT NULL,
  `createdAt` datetime NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `tenancyIdFk` FOREIGN KEY (`tenancyId`) REFERENCES `Tenancy` (`id`)
);

CREATE TABLE `Authorities` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category` varchar(50) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`,`category`,`name`),
  UNIQUE KEY `name_UNIQUE` (`name`)
);

CREATE TABLE `UserAuthorities` (
  `id` int NOT NULL AUTO_INCREMENT,
  `userId` int NOT NULL,
  `authorityId` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `userIdFk_idx` (`userId`),
  KEY `authorityIdFk_idx` (`authorityId`),
  CONSTRAINT `authorityIdFk` FOREIGN KEY (`authorityId`) REFERENCES `Authorities` (`id`) ON DELETE CASCADE,
  CONSTRAINT `userIdFk` FOREIGN KEY (`userId`) REFERENCES `Users` (`id`) ON DELETE CASCADE
);

CREATE TABLE `Verifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(150) NOT NULL,
  `type` varchar(45) NOT NULL,
  `status` varchar(45) NOT NULL,
  `hash` varchar(2000) NOT NULL,
  `lastChange` datetime NOT NULL,
  PRIMARY KEY (`id`)
);
```

### Database initial data

```
INSERT INTO Authorities (category, name) VALUES ('COMMON_USER','ROLE_USER');
INSERT INTO Authorities (category, name) VALUES ('ADMINISTRATOR','ROLE_ADMIN');
INSERT INTO Authorities (category, name) VALUES ('ORGANISATION','TENANCY_ADMIN');
```

## Configurations

### Database connection

* **journal.authentication.datasource.host** *e.g. localhost*
* **journal.authentication.datasource.port** *e.g. 3306*
* **journal.authentication.datasource.database** *e.g. dbname*
* **journal.authentication.datasource.username** *e.g. user*
* **journal.authentication.datasource.password** *e.g. root*

### Email Verification
In case there is need for new users confirm their emails, then enable the configuration property:
* **journal.authentication.verification.enabled** *e.g. true*

### Generic Properties
* **journal.authentication.hosts.front-end** *e.g. http://localhost:8080* to be able to proper redirect the user to the page of confirm registration or change password
* **journal.authentication.hosts.back-end** *e.g. http://localhost:8080* to be able to proper redirect the user confirmation of registration check

## Metrics

### Retrieve reactive metrics

* http://localhost:8080/metrics/signup_user - Amount of time create a new user via signup
* http://localhost:8080/metrics/signin_use - Amount of time to authenticate a user
* http://localhost:8080/metrics/refresh_token - Amount of time to refresh the user token
* http://localhost:8080/metrics/verify_new_user - Amount of time to verify a new user emails
* http://localhost:8080/metrics/send_new_verification - Amount of time to send a new email verification to the user email
* http://localhost:8080/metrics/request_password_change - Amount of time to request a password change
* http://localhost:8080/metrics/password_change - Amount of time to apply a password change
* http://localhost:8080/metrics/get_me_info - Amount of time to retrieve user information