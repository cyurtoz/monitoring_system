package com.cagatay.gameclients.simulation

import java.util.UUID

import com.cagatay.gameclients.data.{ClientEvent, Defaults}

import scala.util.Random

object EventSimulator {

  def simulateEvent(clientId: String, sequenceId: Int): ClientEvent = {

    val event_id = UUID.randomUUID().toString.split("-")(0)

    // create random
    val rand = new Random(System.nanoTime() + sequenceId)

    // pick a random event type - cumulative or time-series
    val eventTypeIndex = rand.nextInt(Defaults.eventTypes.length)
    val eventType: String = Defaults.eventTypes(eventTypeIndex)

    // from event types, pick a random event key
    val eventKeys: List[String] = Defaults.eventTypesToKeys(eventType)
    val eventKeyIndex: Int = rand.nextInt(eventKeys.length)
    val eventKey: String = eventKeys(eventKeyIndex)

    // for event key, pick a random pre-defined event value
    val values: List[Int] = Defaults.eventValues(eventKey)
    val eventValueIndex: Int = rand.nextInt(values.length)
    val eventValue: Int = values(eventValueIndex)

    // return the created event
    ClientEvent(clientId, event_id, System.currentTimeMillis(), eventType, eventKey, eventValue.toString)

  }

}
