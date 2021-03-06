package heron.client

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

object Client extends App {
  implicit val system = ActorSystem("api")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._

  Http()
    .singleRequest(Get("http://localhost:9090/example"))
    .flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
    .foreach(e => e.runForeach(println))

}
