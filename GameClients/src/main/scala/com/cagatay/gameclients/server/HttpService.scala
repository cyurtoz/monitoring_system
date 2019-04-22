package com.cagatay.gameclients.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import com.cagatay.gameclients.data.ClientEvent
import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}

class HttpService(system: ActorSystem) {

  import io.circe.generic.auto._
  import io.circe.syntax._

  implicit val ec: ExecutionContext = ExecutionContext.global

  private val uri = "http://localhost:8080/action"

  def sendEvent(clientEvent: ClientEvent) = {
    val json: Json = clientEvent.asJson
    val req: HttpRequest = new HttpRequest(HttpMethods.POST,
      Uri(uri),
      List(),
      HttpEntity(ContentTypes.`application/json`, json.noSpaces),
      HttpProtocols.`HTTP/1.1`)
    val resp: Future[HttpResponse] = Http(system).singleRequest(req)
    resp
  }


}
