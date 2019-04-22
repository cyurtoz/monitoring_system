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

  /**
    * Validated and saves a client event, or returns an error response.
    * @param cliEvent event that will be saved.
    * @return async response, wrapped in a future.
    */
  def save(cliEvent: ClientEvent): Future[ClientEvent] = {
    // validate the new event
    val validatedEvent = validator.validate(cliEvent)

    validatedEvent match {
        // event has no problems
      case Success(value) =>
        // ask repo to save the event
        val repoResult: Future[RepoResponse] = (eventRepository ? SaveEvent(value)).mapTo[RepoResponse]
        repoResult transform {
          case Success(result) =>
            result match {
              case RepoSuccess(e) => Success(e)  // repo result is successful
              case RepoFail(msg) => Failure(RepoFailException(msg))  // repo result failed, e.g. duplicate event id
            }
          case Failure(exception) => Failure(exception)
        }
      case Failure(exception) => Future.failed(exception)
    }
  }

  def dump(clientId: String): Future[DumpResponse] = {
    // ask repo to give dump of client id
    (eventRepository ? GetDump(clientId)).mapTo[DumpResponse]
  }

  case class RepoFailException(message: String) extends Exception(message)

}
