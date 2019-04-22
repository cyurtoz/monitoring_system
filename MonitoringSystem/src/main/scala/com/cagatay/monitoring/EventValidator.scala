package com.cagatay.monitoring

import scala.util.{Failure, Success, Try}

class EventValidator {

  /**
    * Validates a client event through validator methods.
    *
    * @param e client event comes from user request.
    * @return success wrapper with the event itself, or failure wrapper with the error message.
    */
  def validate(e: ClientEvent): Try[ClientEvent] = {
    List(
      validateEventType(e),
      validateEventKey(e),
      validateIfCumulativeValue(e)).find(_.isFailure).getOrElse(Success(e))
  }

  /**
    * Event key type be fit to event type, and must be declared in pre defined event types.
    *
    * @return
    */
  private def validateEventType: ClientEvent => Try[ClientEvent] = {
    event => if (GameData.eventTypes.contains(event.event_type)) Success(event) else Failure(InvalidType())
  }

  /**
    * Event key must be fit to event type, and must be declared in pre defined event types.
    *
    * @return
    */
  private def validateEventKey: ClientEvent => Try[ClientEvent] = {
    event =>
      if (GameData.eventTypesToKeys.contains(event.event_type)
        && GameData.eventTypesToKeys(event.event_type).contains(event.event_key))
        Success(event) else Failure(InvalidKey())
  }

  /**
    * If event is cumulative, the value must be able to be parsed to Double.
    *
    * @return boolean true or false, if value can be parsed.
    */
  private def validateIfCumulativeValue: ClientEvent => Try[ClientEvent] = {
    event =>
      if (GameData.isCumulative(event) && Try(event.event_value.toDouble).isFailure) {
        Failure(InvalidCumulativeValue())
      } else {
        Success(event)
      }
  }

  case class InvalidType(message: String = "Invalid event type.") extends Exception(message)

  case class InvalidKey(message: String = "Invalid event key.") extends Exception(message)

  case class InvalidCumulativeValue(message: String = "Invalid cumulative value.") extends Exception(message)

}
