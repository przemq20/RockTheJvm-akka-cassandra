package model.bank

sealed trait BankEvent
object BankEvent {
  case class BankAccountCreated(id: String) extends BankEvent
}
