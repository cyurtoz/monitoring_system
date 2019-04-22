package com.cagatay

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.cagatay.monitoring.{EventValidator, MonitoringService, Routes}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main extends LazyLogging {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("main")
    implicit val materializer: ActorMaterializer = ActorMaterializer(None)(system)
    implicit val ec: ExecutionContext = ExecutionContext.global

    val validator = new EventValidator
    val monitoringService = new MonitoringService(system, validator)
    val route = new Routes(monitoringService)

    val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route.routes, "localhost", 8080)
    bindingFuture.onComplete {
      case Success(value) =>
        logger.info("Server started")
      case Failure(exception) =>
        logger.warn("Server failed to start")
        logger.warn(exception.getMessage, exception)
    }

    Unit
  }
}
