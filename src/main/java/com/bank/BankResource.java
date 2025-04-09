package com.bank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Path("/bank")
public class BankResource {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String hello() {
    return "Welcome to the bank";
  }

  @POST
  @Transactional
  @Path("/create")
  public Response create(
      @QueryParam("firstName") String firstName,
      @QueryParam("lastName") String lastName,
      @QueryParam("balance") int balance) {
    BankAccount bankAccount = BankAccount.create(firstName, lastName, balance);
    bankAccount.persist();
    URI location =
        UriBuilder.fromResource(BankResource.class)
            .path("/%s/%s".formatted(firstName, lastName))
            .build();
    try {
      ObjectMapper mapper = new ObjectMapper();
      String jsonString = mapper.writeValueAsString(bankAccount);
      return Response.status(201).entity(jsonString).location(location).build();
    } catch (JsonProcessingException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  @POST
  @Transactional
  @Path("/deposit")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deposit(@QueryParam("name") String name, @QueryParam("balance") int balance) {
    BankAccount bankAccount = BankAccount.findByName(name);
    if (bankAccount == null) {
      return Response.status(404).build();
    }
    bankAccount.increaseBalance(balance);
    ObjectMapper mapper = new ObjectMapper();
    try {
      String jsonString = mapper.writeValueAsString(bankAccount);
      return Response.status(200).entity(jsonString).build();
    } catch (JsonProcessingException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  @GET
  @Transactional
  @Path("/createAccount")
  public String createAccount(
      @QueryParam("firstName") String firstName,
      @QueryParam("lastName") String lastName,
      @QueryParam("balance") int balance) {
    BankAccount account = BankAccount.create(firstName, lastName, balance);
    account.persist();
    return "Account created for %s %s with balance %d".formatted(firstName, lastName, balance);
  }

  @GET
  @Path("accounts")
  public String accounts() {
    List<BankAccount> bankAccounts = BankAccount.listAll();
    return "Accounts created for "
        + bankAccounts.stream()
            .map((bankAccount -> bankAccount.firstName + " " + bankAccount.lastName))
            .collect(Collectors.joining(", "));
  }

  @GET
  @Path("/{firstName}/{lastName}")
  public String getBalance(
      @PathParam("firstName") String firstName, @PathParam("lastName") String lastName) {
    BankAccount bankAccount = BankAccount.findByName(firstName, lastName);
    if (bankAccount == null) {
      return "You do not have a bank account";
    }
    return "Your bank account balance is " + bankAccount.balance;
  }

  @GET
  @Transactional
  @Path("/{firstName}/{lastName}/transfer")
  public String transfer(
      @PathParam("firstName") String firstName,
      @PathParam("lastName") String lastName,
      @QueryParam("receiver") String to,
      @QueryParam("amount") int amount) {
    BankAccount fromAccount = BankAccount.findByName(firstName, lastName);
    BankAccount toAccount = BankAccount.findByName(to);
    if (fromAccount == null) {
      return "You do not have a bank account";
    }
    if (toAccount == null) {
      return to + " does not have a bank account";
    }
    if (fromAccount.balance < amount) {
      return "You do not have enough money";
    }
    fromAccount.deductBalance(amount);
    toAccount.increaseBalance(amount);

    return "Successfully transferred "
        + amount
        + " from "
        + firstName
        + " to "
        + to
        + ". Your balance is now "
        + fromAccount.balance;
  }

  @GET
  @Path("/exchangeRate")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> exchangeRate() throws JsonProcessingException {
    Client client = ClientBuilder.newClient();
    Response response =
        client
            .target("https://v6.exchangerate-api.com/v6/6e31a4a08a1166485013444f/pair/DKK/USD/100")
            .request(MediaType.APPLICATION_JSON)
            .get();

    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readTree(response.readEntity(String.class));
    client.close();
    HashMap<String, Object> map = new HashMap<>();
    map.put("DKK", "100.00");
    map.put("USD", jsonNode.get("conversion_result"));
    return map;
  }
}
