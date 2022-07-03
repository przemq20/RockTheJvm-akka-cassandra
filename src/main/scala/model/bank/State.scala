package model.bank

import akka.actor.typed.ActorRef
import model.bankAccount.Command

case class State(accounts: Map[String, ActorRef[Command]])
