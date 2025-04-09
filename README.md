# Bankdata test


## Start the application in dev mode

Start the application in dev mode:

```shell script
./mvnw quarkus:dev
```
The application is exposed on `http://localhost:8080/bank`


The application is started with two values in the database  
| firstName | lastName | balance | accountNumber |  
| 'Alice'   | 'Doe'    | 100     | 1             |  
| 'Bob'     | 'Doe'    | 100     | 2             |  

## Exposed endpoints 

### Welcome endpoint
http://localhost:8080/bank  
Curl example
```curl
curl -X GET --location "http://localhost:8080/bank"
```

### View Accounts

To get a list of the current customers in the bank send a GET request to the endpoint
```java
@GET
@Path("accounts")
public String accounts()
```
http://localhost:8080/bank/accounts

Curl example 
```curl
curl -X GET --location "http://localhost:8080/bank/accounts"
```
### Creating a new account
To create a new account send a POST request to the endpoint 

```java
@POST 
@Path("/create") 
public Response create(
  @QueryParam("firstName") String firstName,
  @QueryParam("lastName") String lastName,
  @QueryParam("balance") int balance
)
```
Example url
```http request
http://localhost:8080/bank/create?firstName=John&lastName=Doe&balance=100
```
```curl
curl -X POST --location "http://localhost:8080/bank/create?firstName=John&lastName=Doe&balance=100"
```


Version using GET  
http://localhost:8080/bank/createAccount?firstName=John&lastName=Doe&balance=100

### To deposit into an existing account
To add to the balance of an existing account use a POST request to the endpoint
```java
@POST 
@Path("/deposit") 
@Produces(MediaType.APPLICATION_JSON) 
public Response deposit(
  @QueryParam("name") String name,
  @QueryParam("balance") int balance
)
```
where `name` is the concatenated full name and `balance` is the amount to add.  

Curl example 
```curl
curl -X POST --location "http://localhost:8080/bank/deposit?name=AliceDoe&balance=10"
```

### To view balance of account
To view the balance of an account, lookup bye the first and last name http://localhost:8080/bank/Alice/Doe
```java
@GET 
@Path("/{firstName}/{lastName}") 
public String getBalance(
  @PathParam("firstName") String firstName,
  @PathParam("lastName") String lastName
)
```

Curl example
```curl
curl -X GET --location "http://localhost:8080/bank/Alice/Doe"
```
### To transfer between two accounts
To transfer between two accounts use the endpoint
```java
@GET 
@Path("/{firstName}/{lastName}/transfer") 
public String transfer(
  @PathParam("firstName") String firstName,
  @PathParam("lastName") String lastName,
  @QueryParam("receiver") String to,
  @QueryParam("amount") int amount
)
```
The sender is indexed by firstName and lastName while the receiver is indexed by firstNameLastName.  
Example http://localhost:8080/bank/Alice/Doe/transfer?receiver=BobDoe&amount=10 

Curl example
```curl 
curl -X GET --location "http://localhost:8080/bank/Alice/Doe/transfer?receiver=BobDoe&amount=10"
```

### To get the exchange rate for USD

```java
@GET 
@Path("/exchangeRate") 
@Produces(MediaType.APPLICATION_JSON) 
public Map<String, Object> exchangeRate()
throws JsonProcessingException
```
http://localhost:8080/bank/exchangeRate 

Curl example 
```curl
curl -X GET --location "http://localhost:8080/bank/exchangeRate"
```