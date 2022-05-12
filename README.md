# Trading Journal Authentication

## Pending

* Authorize users/requests
* Me information endpoint
* Private key/certificate for token key
  * Application properties
    * add expiration for token
* Refresh token mechanism
* Change password
* Email verification
* Admin on start up
  * Must change password
* Admin endpoints
* Interface for Application User/Application User Repository (and other Entities if needed)
  * This should allow user storage to be in another DB then MongoDB
* Delete account
* Lib for token validation to be used in other projects
  * Consider:
    * Token validation
    * Token reader
    * With Private key/certificate
* Use token generated here in another project to validate flow and Lib (above)
* Create version 1.0.0
* Create TAG with current code for reuse in other projects

<http://localhost:8080/swagger-ui/index.html>

docker run -p 27017:27017 --name mongo -d mongo
