# Trading Journal Authentication

## Pending

* Refresh token mechanism
  * Claims - check other claims names
* Roles Interface, can be Static or a Entity
* Secure token better
  * Change some constant values to configuration
  * Add more validations to token valid such issuer, audience, etc.
* Change password
* Email verification
* Admin on start up
  * Must change password
* Admin endpoints
  * Improve SecurityConfigurationTest with admin access
* Postman Test run
* Test Coverage with fail under X percent
* Interface for Application User/Application User Repository (and other Entities if needed)
  * This should allow user storage to be in another DB then MongoDB
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
docker run -p 27017:27017 --name mongo -d mongo
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
