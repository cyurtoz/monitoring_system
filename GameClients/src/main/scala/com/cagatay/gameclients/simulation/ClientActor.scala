package com.cagatay.gameclients.simulation

import java.util.UUID

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.HttpResponse
import com.cagatay.gameclients.data.ClientEvent
import com.cagatay.gameclients.server.HttpService
import com.cagatay.gameclients.simulation
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Random, Success}

class ClientActor(minInterval: Int, maxInterval: Int, httpService: HttpService, sequenceId: Int)
  extends Actor with LazyLogging {

  // a random client id that is assigned to the each client.
  // uuid string is shortened because it was dominating the logs
  private val clientId = UUID.randomUUID().toString.split("-")(0)

  logger.info(s"Actor with name ${self.path.name} created. Client id $clientId")

  implicit val ec: ExecutionContextExecutor = context.dispatcher

  // events that were successfully sent to the server will be saved in this set, local to each client
  // it is necessary to verify the system after shutdown
  private var sentEvents: Set[ClientEvent] = Set.empty

  // start scheduling events by sending a schedule message to the client itself.
  override def preStart(): Unit = {
    self ! ScheduleEvent
  }

  override def receive: Receive = {
    case SendEvent(event) =>

      // send an event to the server
      val resp: Future[HttpResponse] = httpService.sendEvent(event)
      resp.onComplete {
        case Success(value) =>
          if (value.status.isSuccess()) {
            // server response is successful, save the event and log
            sentEvents += event
            logger.info(s"Event sent: client id $clientId event: $event")
          } else {
            // server response is not success
            logger.warn(s"Sending event failed. Status code is ${value.status.value}")
          }
        case Failure(exception) =>
          // another exception happened
          logger.warn(s"Sending event failed", exception)
      }
      // schedule new event to
      self ! ScheduleEvent

    case ScheduleEvent =>
      // create a new event, sequence id is needed for random seed
      val event = EventSimulator.simulateEvent(clientId, sequenceId)

      // create a random time interval for the next event
      val rand = new Random(System.currentTimeMillis() + sequenceId)
      val nextEventTime = rand.nextInt(maxInterval - minInterval) + minInterval

      // schedule the next event
      // a sendEvent message will be sent to this actor from the scheduler
      context.system.scheduler.scheduleOnce(nextEventTime.seconds, self, simulation.SendEvent(event))

  }
}

object ClientActor {
  def props(minInterval: Int, maxInterval: Int, httpService: HttpService, sequenceId: Int)
  = Props(new ClientActor(minInterval, maxInterval, httpService, sequenceId))

  def name(sequenceId: Int) = s"client-actor-$sequenceId"
}

case class SendEvent(clientEvent: ClientEvent)

case object ScheduleEvent