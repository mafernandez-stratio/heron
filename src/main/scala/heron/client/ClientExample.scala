package heron.client

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

object ClientExample extends App {
  import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._

  implicit val system = ActorSystem("client")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  Http()
    .singleRequest(Get("http://localhost:9090/events"))
    .flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
    .foreach(_.runForeach(println))

  system.terminate()
}
