# Trading Journal Authentication

## Pending

* Role/Authorities
  * Roles can be Static or an Entity
  * Create an Enum with basic Roles (USER and ADMIN) with Category enum as well
    * If Static, just relly on Enum
    * If Entity, save all enums in db on startup
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
