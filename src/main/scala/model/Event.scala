package model

trait Event
case class BankAccountCreated(bankAccount: BankAccount) extends Event
case class BalanceUpdated(amount:          BigDecimal) extends Event
