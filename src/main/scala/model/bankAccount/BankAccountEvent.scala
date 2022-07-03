package model.bankAccount

trait BankAccountEvent
object BankAccountEvent {
  case class BankAccountCreated(bankAccount: BankAccount) extends BankAccountEvent
  case class BalanceUpdated(amount:          BigDecimal) extends BankAccountEvent
}
