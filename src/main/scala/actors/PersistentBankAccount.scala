package actors

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior }
import model.bankAccount.Command._
import model.bankAccount.BankAccountEvent._
import model.bankAccount.Response._
import model.bankAccount.{ BankAccount, BankAccountEvent, Command }

object PersistentBankAccount {

  val commandHandler: (BankAccount, Command) => Effect[BankAccountEvent, BankAccount] = (state, command) =>
    command match {
      case CreateBankAccount(user, currency, initialBalance, replyTo) =>
        val id          = state.id
        val bankAccount = BankAccount(id, user, currency, initialBalance)
        Effect.persist(BankAccountCreated(bankAccount)).thenReply(replyTo)(_ => BankAccountCreatedResponse(id))
      case UpdateBalance(id, currency, amount, replyTo) =>
        val newBalance = state.balance + amount
        if (newBalance < 0)
          Effect.reply(replyTo)(BankAccountBalanceUpdatedResponse(None))
        else {
          Effect
            .persist(BalanceUpdated(amount))
            .thenReply(replyTo)(newState => BankAccountBalanceUpdatedResponse(Some(newState)))
        }
      case GetBankAccount(id, replyTo) => Effect.reply(replyTo)(GetBankAccountResponse(Some(state)))
    }

  val eventHandler: (BankAccount, BankAccountEvent) => BankAccount = (state, event) =>
    event match {
      case BankAccountCreated(bankAccount) => bankAccount
      case BalanceUpdated(amount)          => state.copy(balance = state.balance + amount)
    }

  def apply(id: String): Behavior[Command] =
    EventSourcedBehavior[Command, BankAccountEvent, BankAccount](
      persistenceId  = PersistenceId.ofUniqueId(id),
      emptyState     = BankAccount(id, "", "", 0.0),
      commandHandler = commandHandler,
      eventHandler   = eventHandler
    )
}
