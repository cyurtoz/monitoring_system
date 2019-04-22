package com.cagatay.gameclients

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.cagatay.gameclients.data.Defaults
import com.cagatay.gameclients.server.HttpService
import com.cagatay.gameclients.simulation.ClientActor
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

object Main extends LazyLogging {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("main")
    implicit val materializer: ActorMaterializer = ActorMaterializer(None)(system)
    implicit val ec: ExecutionContext = ExecutionContext.global

    val clients = Option(System.getProperty("clients"))
    val minInterval = Option(System.getProperty("minInterval"))
    val maxInterval = Option(System.getProperty("maxInterval"))

    val numberOfClients: Int = clients match {
      case None => Defaults.numberOfClients
      case Some(vll) => if (vll.isEmpty) Defaults.numberOfClients else vll.toInt
    }

    val minIntervalSeconds: Int = minInterval match {
      case None => Defaults.minIntervalInSeconds
      case Some(vll) => if (vll.isEmpty) Defaults.minIntervalInSeconds else vll.toInt
    }

    val maxIntervalSeconds: Int = maxInterval match {
      case None => Defaults.maxIntervalInseconds
      case Some(vll) => if (vll.isEmpty) Defaults.maxIntervalInseconds else vll.toInt
    }

    if (minIntervalSeconds > maxIntervalSeconds) {
      throw new Exception("max interval must be greater than min.")
    } else {
      val httpService: HttpService = new HttpService(system)

      for (i <- 1 to numberOfClients) {
        system.actorOf(ClientActor.props(minIntervalSeconds, maxIntervalSeconds, httpService, i), ClientActor.name(i))
      }
    }



  }
}
