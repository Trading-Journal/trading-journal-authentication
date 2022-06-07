# Trading Journal Authentication

## Pending

* Remove Reactive and make it normal rest api
  * Redo the metrics and document them
* Admin on start up
  * Must change password - Registration and Change password must be only one email doing both at the same time
* Admin endpoints
  * Improve SecurityConfigurationTest with admin access
  * Manage User
    * Disable
    * Delete
    * Change authorities
* Manage Authorities via API when Database authorities (ConditionalOnProperty???)
  * Admin access only
  * Validate if entity authorities is enabled before manage
  * Manage user authorities
* Postman Test run
* Test Coverage with fail under X percent
* Delete account
* Set and document environment variables
* Test Container/Kubernetes deploy with adding keys files
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

[Localhost swagger URL](http://localhost:8080/swagger-ui/index.html)

## Running

### Container Dependencies

#### MySql

```bash
docker run -d -e MYSQL_USER=trade-journal -e MYSQL_PASSWORD=trade-journal -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=trade-journal -p 3306:3306 mysql:latest
```

#### SMTP Server

```bash
docker run -d -p 587:587 --name mail \
    -e RELAY_HOST=smtp.example.com \
    -e RELAY_PORT=587 \
    -e RELAY_USERNAME=alice@example.com \
    -e RELAY_PASSWORD=secretpassword \
    -d bytemark/smtp
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
CREATE TABLE `Users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `userName` varchar(45) NOT NULL,
  `password` varchar(2000) NOT NULL,
  `firstName` varchar(45) NOT NULL,
  `lastName` varchar(45) NOT NULL,
  `email` varchar(150) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  `verified` tinyint(1) NOT NULL,
  `createdAt` datetime NOT NULL,
  `authorities` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
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
  `authorityId` int DEFAULT NULL,
  `name` varchar(45) NOT NULL,
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

## Configurations

### Database connection

* **journal.authentication.datasource.host** *e.g. localhost*
* **journal.authentication.datasource.port** *e.g. 3306*
* **journal.authentication.datasource.database** *e.g. dbname*
* **journal.authentication.datasource.username** *e.g. user*
* **journal.authentication.datasource.password** *e.g. root*

### Handle Authority/Roles

There are two ways to handle Authority/Roles:
* STATIC: No authorities are persisted in the database, there are initially two possible roles defined in the file **AuthoritiesHelper** **ROLE_USER** and **ROLE_ADMIN**
* DATABASE: Authorities will be persisted and retrieved from database, a initial load is made in the table  **Authorities** with roles defined in the file **AuthoritiesHelper** **ROLE_USER** and **ROLE_ADMIN**
  This configuration can be changed using the property **journal.authentication.authority.type** with none is defined, the default behavior is **STATIC**
* **journal.authentication.authority.type** *e.g. STATIC*
* **journal.authentication.authority.type** *e.g. DATABASE*

### Email Verification
In case there is need for new users confirm their emails, then enable the configuration property:
* **journal.authentication.verification.enabled** *e.g. true*

### Generic Properties
* **journal.authentication.hosts.front-end** *e.g. http://localhost:8080* to be able to proper redirect the user to the page of confirm registration or change password
* **journal.authentication.hosts.back-end** *e.g. http://localhost:8080* to be able to proper redirect the user confirmation of registration check

## Metrics

### Retrieve reactive metrics

* http://localhost:8080/metrics/signup_user.flow.duration - Data about Signing Up process
* http://localhost:8080/metrics/create_new_user.flow.duration - Data about Creating a new User process
* http://localhost:8080/metrics/signing_user.flow.duration - Data about Signing In process
* http://localhost:8080/metrics/refresh_token.flow.duration - Data about Refreshing Token process
* http://localhost:8080/metrics/get_me_info.flow.duration - Data about Getting Current User Information process
* http://localhost:8080/metrics/verify_new_user.flow.duration - Data about Email Verification for new users
* http://localhost:8080/metrics/password_change_request.flow.duration - Data about requester for a password change
* http://localhost:8080/metrics/password_change.flow.duration - Data about effectively change the user password