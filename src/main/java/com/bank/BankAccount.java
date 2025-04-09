package com.bank;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

@Entity
public class BankAccount extends PanacheEntity {

  public String firstName;
  public String lastName;
  public int balance;

  protected BankAccount() {}

  BankAccount(String firstName, String lastName, int balance) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.balance = balance;
  }

  public static BankAccount create(String firstName, String lastName, int balance) {
    return new BankAccount(firstName, lastName, balance);
  }

  public long getAccountNumber() {
    return id;
  }

  public static BankAccount findByName(String firstName, String lastName) {
    return find("firstName = ?1 and lastName= ?2" , firstName, lastName).firstResult();
  }

  public static BankAccount findByName(String fullName) {
    return find("concat(firstName,lastName) = ?1", fullName).firstResult();
  }

  @Transactional
  public void deductBalance(int amount) {
    balance -= amount;
  }

  @Transactional
  public void increaseBalance(int amount) {
    balance += amount;
  }
}
