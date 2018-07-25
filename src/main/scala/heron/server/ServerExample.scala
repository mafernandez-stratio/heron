package heron.server

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.stream.scaladsl.Source

import scala.io.StdIn
import scala.concurrent.duration._

class MyIterator extends Iterator[String](){
  var count = 0

  override def hasNext: Boolean = {
    if(count<10){
      true
    } else {
      false
    }

  }

  override def next(): String = {
    if(!hasNext){
      return null
    }
    count = count+1
    Thread.sleep(2000)
    s"($count) ${UUID.randomUUID().toString}"
  }
}

object ServerExample extends App {
  import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._

  implicit val system = ActorSystem("server")
  implicit val materializer = ActorMaterializer(/*ActorMaterializerSettings(system)
    .withInputBuffer(
      initialSize = 1,
      maxSize = 2)*/)
  implicit val executionContext = system.dispatcher

  val iter = new MyIterator()

  val route = {
    path("events") {
      get {
        complete {
          /*Source
            .tick(2.seconds, 2.seconds, NotUsed)
            .map(_ => LocalTime.now())
            .map(time => ServerSentEvent(ISO_LOCAL_TIME.format(time)))
            .keepAlive(1.second, () => ServerSentEvent.heartbeat)*/
          Source.fromIterator(() => new MyIterator()).map(e => ServerSentEvent(e)).async
          /*Source
            .tick(2.seconds, 2.seconds, NotUsed)
            .map(time => ServerSentEvent(iter.next()))
            .keepAlive(1.second, () => ServerSentEvent.heartbeat)*/
        }
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 9090)
  println("Example Server started at localhost:9090")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())

}
