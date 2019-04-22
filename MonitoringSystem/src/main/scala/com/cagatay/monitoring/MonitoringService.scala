package com.cagatay.monitoring

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.cagatay.monitoring.repo.{DumpResponse, EventRepositoryActor, GetDump, RepoFail, RepoResponse, RepoSuccess, SaveEvent}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class MonitoringService(system: ActorSystem, validator: EventValidator) {

  val eventRepository: ActorRef = system.actorOf(EventRepositoryActor.props, EventRepositoryActor.name)

  implicit val repoTimeout: Timeout = Timeout(5.seconds)
  implicit val ec: ExecutionContext = ExecutionContext.global

  def save(cliEvent: ClientEvent): Future[ClientEvent] = {
    val validatedEvent = validator.validate(cliEvent)
    validatedEvent match {
      case Success(value) =>
        val repoResult: Future[RepoResponse] = (eventRepository ? SaveEvent(value)).mapTo[RepoResponse]
        repoResult transform {
          case Success(result) =>
            result match {
              case RepoSuccess(e) => Success(e)
              case RepoFail(msg) => Failure(RepoFailException(msg))
            }
          case Failure(exception) => Failure(exception)
        }
      case Failure(exception) => Future.failed(exception)
    }
  }

  def dump(clientId: String): Future[DumpResponse] = {
    (eventRepository ? GetDump(clientId)).mapTo[DumpResponse]
  }

  case class RepoFailException(message: String) extends Exception(message)

}
