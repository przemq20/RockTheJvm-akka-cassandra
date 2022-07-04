package http

import akka.actor.typed.ActorSystem
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import model.bankAccount.{ Command, Response }
import model.bankAccount.Command._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import model.bankAccount.Response.{ BankAccountBalanceUpdatedResponse, BankAccountCreatedResponse, GetBankAccountResponse }
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Future

case class BankAccountCreationRequest(user: String, currency: String, balance: Double) {
  def toCommand(replyTo: ActorRef[Response]): Command = CreateBankAccount(user, currency, balance, replyTo)
}

case class FailureResponse(reason: String)

case class BankAccountUpdateRequest(currency: String, balance: Double) {
  def toCommand(id: String, replyTo: ActorRef[Response]): Command = UpdateBalance(id, currency, balance, replyTo)
}

class BankRouter(bank: ActorRef[Command])(implicit system: ActorSystem[_]) {

  implicit val timeout: Timeout = Timeout(5.seconds)

  def createBankAccount(request: BankAccountCreationRequest): Future[Response] =
    bank.ask(replyTo => request.toCommand(replyTo))

  def getBankAccount(id: String): Future[Response] =
    bank.ask(replyTo => GetBankAccount(id, replyTo))

  def updateBankAccount(id: String, request: BankAccountUpdateRequest): Future[Response] =
    bank.ask(replyTo => request.toCommand(id, replyTo))

  val routes: Route =
    pathPrefix("bank") {
      pathEndOrSingleSlash {
        post {
          entity(as[BankAccountCreationRequest]) { request =>
            onSuccess(createBankAccount(request)) {
              case BankAccountCreatedResponse(id) =>
                respondWithHeader(Location(s"/bank/$id")) {
                  complete(StatusCodes.Created)
                }
            }
          }
        }
      } ~
        path(Segment) { id =>
          get {
            onSuccess(getBankAccount(id)) {
              case GetBankAccountResponse(Some(account)) =>
                complete(account)
              case GetBankAccountResponse(None) =>
                complete(StatusCodes.NotFound, FailureResponse(s"Bank account $id not found"))
            }
          } ~
            put {
              entity(as[BankAccountUpdateRequest]) { request =>
                onSuccess(updateBankAccount(id, request)) {
                  case BankAccountBalanceUpdatedResponse(Some(account)) =>
                    complete(account)
                  case BankAccountBalanceUpdatedResponse(None) =>
                    complete(StatusCodes.NotFound, FailureResponse(s"Bank account $id not found"))
                }
              }
            }
        }
    }
}
