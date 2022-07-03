package model.bankAccount

import akka.actor.typed.ActorRef

sealed trait Command
object Command {
  case class CreateBankAccount(user: String, currency: String, initialBalance: BigDecimal, replyTo: ActorRef[Response])
      extends Command
  case class UpdateBalance(id:  String, currency: String, amount: BigDecimal, replyTo: ActorRef[Response]) extends Command
  case class GetBankAccount(id: String, replyTo:  ActorRef[Response]) extends Command
}
