package model.bankAccount

sealed trait Response
object Response {
  case class BankAccountCreatedResponse(id:                      String) extends Response
  case class BankAccountBalanceUpdatedResponse(maybeBankAccount: Option[BankAccount]) extends Response
  case class GetBankAccountResponse(maybeBankAccount:            Option[BankAccount]) extends Response
}
