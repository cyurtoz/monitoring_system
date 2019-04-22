package com.cagatay.monitoring

import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.extras.Configuration

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

final class Routes(monitoringService: MonitoringService)
  extends Directives with LazyLogging with FailFastCirceSupport {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val configuration: Configuration = Configuration.default
    .withDiscriminator("type")

  import io.circe.generic.extras.auto._

  val routes: Route =
    path("") {
      complete("Monitoring System")
    } ~
      path("action") {
        post {
          entity(as[ClientEvent]) {
            e =>
              val triedEvent: Future[ClientEvent] = monitoringService.save(e)
              val g: Future[ApiResponse] = triedEvent.transformWith {
                case Success(respp) => Future.successful(SuccessResponse(body = respp))
                case Failure(exception) => Future.successful(FailedResponse(errorMsg = exception.getMessage))
              }
              complete(g)
          }
        }
      } ~
      path("dump") {
        get {
          parameters("client_id") {
            param =>
              complete(monitoringService.dump(param))
          }
        }
      }

}

case class ClientEvent(client_id: String, event_id: String, event_timestamp: Long,
                       event_type: String, event_key: String, event_value: String)

sealed trait ApiResponse

case class SuccessResponse(code: Int = 200, body: ClientEvent) extends ApiResponse

case class FailedResponse(code: Int = 400, errorMsg: String) extends ApiResponse
