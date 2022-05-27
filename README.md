# Trading Journal Authentication

## Pending

* Add name and metrics for all Mono/Flux
* Secure token better
  * Rename JWT Properties to something meaningful and generic 
  * Change some constant values to configuration
* Change password
* Email verification
* Admin on start up
  * Must change password
* Admin endpoints
  * Improve SecurityConfigurationTest with admin access
  * Manage User
    * Disable
    * Delete
    * Change authorities
  * Manage Authorities via API
    * Admin access only
    * Validate if entity authorities is enabled before manage
  * Manage user authorities
* Postman Test run
* Test Coverage with fail under X percent
* Delete account
* Test Container/Kubernetes deploy with adding keys files
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
CREATE TABLE `Users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `userName` varchar(45) NOT NULL,
  `password` varchar(2000) NOT NULL,
  `firstName` varchar(45) NOT NULL,
  `lastName` varchar(45) NOT NULL,
  `email` varchar(150) NOT NULL,
  `enabled` tinyint NOT NULL,
  `verified` tinyint NOT NULL,
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
)
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