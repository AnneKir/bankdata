package com.bank;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.json.Json;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class BankResourceTest {

  @Test
  void testEntryEndpoint() {
    given().when().get("/bank").then().statusCode(200).body(is("Welcome to the bank"));
  }

  @Test
  void testAccountsEndpoint() {
    given()
        .when()
        .get("bank/accounts")
        .then()
        .statusCode(200)
        .body(is("Accounts created for Alice Doe, Bob Doe"));
  }

  @Test
  @Transactional
  void testCreateAccountEndpoint() {
    given()
        .queryParam("firstName", "John")
        .queryParam("lastName", "Doe")
        .queryParam("balance", "100")
        .when()
        .get("/bank/createAccount")
        .then()
        .statusCode(200)
        .body(containsString("Account created for John Doe with balance 100"));
    // Restore
    BankAccount.delete("firstName", "John");
  }

  @Test
  void testGetBalanceEndpoint() {
    given()
        .pathParam("firstName", "Alice")
        .pathParam("lastName", "Doe")
        .when()
        .get("/bank/{firstName}/{lastName}")
        .then()
        .statusCode(200)
        .body(containsString("Your bank account balance is "));
    given()
        .pathParam("firstName", "Carl")
        .pathParam("lastName", "Doe")
        .when()
        .get("/bank/{firstName}/{lastName}")
        .then()
        .statusCode(200)
        .body(is("You do not have a bank account"));
  }

  @Test
  void testFindByName() {
    BankAccount alice1 = BankAccount.findByName("AliceDoe");
    BankAccount alice2 = BankAccount.findByName("Alice", "Doe");
    assertEquals(alice1.id, alice2.id);
    BankAccount bob1 = BankAccount.findByName("Bob", "Doe");
    BankAccount bob2 = BankAccount.findByName("BobDoe");
    assertEquals(bob2.id, bob1.id);
  }

  @Test
  @Transactional
  void testTransferEndpoint() {
    BankAccount bankAccountAlice = BankAccount.findByName("Alice", "Doe");
    assertNotNull(bankAccountAlice);
    BankAccount bankAccountBob = BankAccount.findByName("Bob", "Doe");
    assertNotNull(bankAccountBob);
    assertEquals(100, bankAccountAlice.balance);
    assertEquals(100, bankAccountBob.balance);
    given()
        .pathParam("firstName", "Alice")
        .pathParam("lastName", "Doe")
        .queryParam("amount", "10")
        .queryParam("receiver", "BobDoe")
        .when()
        .get("/bank/{firstName}/{lastName}/transfer/")
        .then()
        .statusCode(200)
        .body(containsString("Successfully transferred"));
    BankAccount.getEntityManager().clear();
    assertEquals(90, BankAccount.findByName("Alice", "Doe").balance);
    assertEquals(110, BankAccount.findByName("Bob", "Doe").balance);

    // Restore
    BankAccount.findByName("AliceDoe").increaseBalance(10);
    BankAccount.findByName("BobDoe").deductBalance(10);
  }

  @Test
  @Transactional
  void testDepositEndpoint() {
    BankAccount bankAccountAlice = BankAccount.findByName("Alice", "Doe");
    assertEquals(100, bankAccountAlice.balance);
    Response response =
        given()
            .queryParam("name", "AliceDoe")
            .queryParam("balance", "10")
            .when()
            .post("/bank/deposit")
            .then()
            .extract()
            .response();
    assertEquals(200, response.getStatusCode());
    assertEquals("Alice", response.jsonPath().getString("firstName"));

    BankAccount.getEntityManager().clear();
    BankAccount account = BankAccount.findByName("Alice", "Doe");
    assertEquals(110, account.balance);

    //Restore
    BankAccount.findByName("AliceDoe").deductBalance(10);
  }
}
