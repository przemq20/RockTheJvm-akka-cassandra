package actors

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior }
import model.bankAccount.Command
import model.bank.BankEvent
import model.bank.BankEvent.BankAccountCreated
import model.bank.State
import model.bankAccount.Command.{ CreateBankAccount, GetBankAccount, UpdateBalance }
import model.bankAccount.Response.{ BankAccountBalanceUpdatedResponse, GetBankAccountResponse }

import java.util.UUID

object Bank {
  def commandHandler(context: ActorContext[Command]): (State, Command) => Effect[BankEvent, State] =
    (state, command) =>
      command match {
        case createCommand @ CreateBankAccount(user, currency, initialBalance, replyTo) =>
          val id             = UUID.randomUUID().toString
          val newBankAccount = context.spawn(PersistentBankAccount(id), id)
          Effect
            .persist(BankAccountCreated(id))
            .thenReply(newBankAccount)(_ => createCommand)

        case updateCommand @ UpdateBalance(id, currency, amount, replyTo) =>
          state.accounts.get(id) match {
            case Some(account) =>
              Effect.reply(account)(updateCommand)
            case None =>
              Effect.reply(replyTo)(BankAccountBalanceUpdatedResponse(None))
          }
        case getCommand @ GetBankAccount(id, replyTo) =>
          state.accounts.get(id) match {
            case Some(account) =>
              Effect.reply(account)(getCommand)
            case None =>
              Effect.reply(replyTo)(GetBankAccountResponse(None))
          }
      }
  val eventHandler: (State, BankEvent) => State = ???

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    EventSourcedBehavior[Command, BankEvent, State](
      persistenceId  = PersistenceId.ofUniqueId("bank"),
      emptyState     = State(Map()),
      commandHandler = commandHandler(context),
      eventHandler   = eventHandler
    )
  }
}
