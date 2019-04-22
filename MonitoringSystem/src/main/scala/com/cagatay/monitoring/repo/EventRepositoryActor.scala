package com.cagatay.monitoring.repo

import akka.actor.{Actor, Props}
import com.cagatay.monitoring.{ClientEvent, GameData}
import com.typesafe.scalalogging.LazyLogging

class EventRepositoryActor extends Actor with LazyLogging {

  private var repo: Map[String, ClientEvent] = Map.empty

  override def receive: Receive = {

    case SaveEvent(event) =>
      if (repo.contains(event.event_id)) {
        sender() ! RepoFail("Duplicate event id. Event will not be stored.")
      } else {
        repo += (event.event_id -> event)
        sender() ! RepoSuccess(event)
      }

    case GetDump(clientId) =>
      // filter all stored events by client id:
      val clientEvents: Map[String, ClientEvent] = repo.filter(_._2.client_id.equals(clientId))

      // partition the events by cumulative and non cumulative
      val (cumulative, timeSeries) = clientEvents.partition(x => GameData.isCumulative(x._2))

      // group time-series by event key:
      val timeSeriesDataGrouped: Map[String, Iterable[ClientEvent]]
      = timeSeries.values.groupBy(_.event_key)

      // then, map time series  to event data class, which contains event id, value and timeStamp
      val timeSeriesDumpData: Map[String, Iterable[TimeSeriesDump]] = timeSeriesDataGrouped.map(group => group._1 ->
        group._2.map(mapToTimeSeriesDump))

      // group cumulative data by event key:
      val cumulativeData: Map[String, Iterable[ClientEvent]] = cumulative.values.groupBy(_.event_key)

      // map to event data class
      val mapped: Map[String, CumulativeDump] = cumulativeData.map(group => group._1 -> mapToCumulativeDump(group._2.toList))

      val resp = DumpResponse(timeSeriesDumpData, mapped)
      sender() ! resp

  }

  private def mapToTimeSeriesDump: ClientEvent => TimeSeriesDump =
    cliEvent => TimeSeriesDump(cliEvent.event_id, cliEvent.event_value, cliEvent.event_timestamp)

  private def mapToCumulativeDump(events: List[ClientEvent]): CumulativeDump = {
    val sorted = events.sortBy(_.event_timestamp)
    val total: Double = aggregateCumulative(events)
    CumulativeDump(total.toString, events.size.toString, sorted.last.event_timestamp)
  }

  private def aggregateCumulative(events: Iterable[ClientEvent]): Double = events.map(_.event_value.toDouble).sum
}

object EventRepositoryActor {
  def props = Props(new EventRepositoryActor)
  def name = "repository-actor"
}

// messages this repo can handle
case class SaveEvent(event: ClientEvent)
case class GetDump(id: String)

// responses
case class DumpResponse(timeSeries: Map[String, Iterable[TimeSeriesDump]], cumulative: Map[String, CumulativeDump])
case class TimeSeriesDump(eventId: String, value: String, timestamp: Long)
case class CumulativeDump(total: String, elementsCount: String, last_updated: Long)

// responses that this repo returns
sealed trait RepoResponse
case class RepoSuccess(event: ClientEvent) extends RepoResponse
case class RepoFail(msg: String) extends RepoResponse

