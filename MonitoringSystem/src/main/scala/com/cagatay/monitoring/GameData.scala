package com.cagatay.monitoring

object GameData {

  val eventTypes = List("CUMULATIVE", "TIME-SERIES")

  val eventTypesToKeys = Map(
    "CUMULATIVE" -> List("enemy_killed", "coin_collected"),
    "TIME-SERIES" -> List("pow_rescued", "enemy_killed", "fruit_eaten", "gun_collected"))

  def isCumulative(e: ClientEvent): Boolean = e.event_type.equals("CUMULATIVE")

}
