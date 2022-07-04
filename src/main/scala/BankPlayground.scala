import actors.Bank
import akka.NotUsed
import akka.actor.typed.{ ActorSystem, Behavior, Scheduler }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import model.bankAccount.Command.{ CreateBankAccount, GetBankAccount }
import model.bankAccount.Response
import model.bankAccount.Response.{ BankAccountCreatedResponse, GetBankAccountResponse }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
object BankPlayground {
  def main(args: Array[String]): Unit = {
    val rootBehaviour: Behavior[NotUsed] = Behaviors.setup { context =>
      val bank   = context.spawn(Bank(), "bank")
      val logger = context.log
      implicit val timeout:          Timeout          = Timeout(2.seconds)
      implicit val scheduler:        Scheduler        = context.system.scheduler
      implicit val executionContext: ExecutionContext = context.executionContext

      val responseHandler = context.spawn(
        Behaviors.receiveMessage[Response] {
          case BankAccountCreatedResponse(id) =>
            logger.info(s"Successfully created bank Account $id")
            Behaviors.same
          case GetBankAccountResponse(maybeBankAccount) =>
            logger.info(s"$maybeBankAccount")
            Behaviors.same
        },
        "replyHandler"
      )

//      bank
//        .ask(replyTo => CreateBankAccount("Przemek", "USD", 10, replyTo))
//        .flatMap {
//          case BankAccountCreatedResponse(id) =>
//            logger.info(s"Created $id")
//            bank.ask(replyTo => GetBankAccount(id, replyTo))
//        }
//        .foreach {
//          case GetBankAccountResponse(maybeBankAccount) =>
//            logger.info(s"$maybeBankAccount")
//        }

//      bank ! CreateBankAccount("Przemek", "PLN", 10, responseHandler)
      bank ! GetBankAccount("d49bcc3e-5ebb-4529-b930-fec398dd6c7b", responseHandler)

      Behaviors.empty
    }

    val system = ActorSystem(rootBehaviour, "Demo")
  }
}
