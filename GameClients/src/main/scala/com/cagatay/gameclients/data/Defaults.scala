package com.cagatay.gameclients.data

object Defaults {

  val minIntervalInSeconds = 3

  val maxIntervalInseconds = 10

  val numberOfClients = 5

  val eventTypes = List("CUMULATIVE", "TIME-SERIES")

  val eventTypesToKeys = Map(
    "CUMULATIVE" -> List(
      "enemy_killed",
      "coin_collected"),
    "TIME-SERIES" -> List(
      "pow_rescued",
      "enemy_killed",
      "fruit_eaten",
      "gun_collected"))


  val eventValues: Map[String, List[Int]] = Map(
    "enemy_killed" -> (1 to 3).toList,
    "coin_collected" -> (3 to 10).toList,
    "pow_rescued" -> (1 to 3).toList,
    "fruit_eaten" -> (1 to 1).toList,
    "gun_collected" -> (1 to 1).toList,

  )

}
