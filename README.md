# Trading Journal Authentication

## Pending

* Role tenancy (Company) Administrator
  * Add the MockSecurity again
  * Can add or remove users - based on tenancy limits (missing integrated tests)
    * New user should change password
  * Endpoint to retrieve tenancy limits for the organisation access
* Change me endpoint to update user data
* Authorities endpoints
  * Enable access to  ORGANISATION user
  * Do not manage system admin when category when it is an ORGANISATION user
  * Manage all when logged user id ADMINISTRATOR
* Delete account
  * Delete common user
  * Delete tenancy (Company) admin user:
    * Set another user as admin
    * Or remove the whole tenancy (Company)
* Set and document environment variables and properties
* Test Container/Kubernetes deploy with adding keys files
* Postman Test run
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

## Swagger

[http://localhost:8080/swagger-ui/index.htm](http://localhost:8080/swagger-ui/index.html)
Or just [http://localhost:8080](http://localhost:8080)

## Running

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