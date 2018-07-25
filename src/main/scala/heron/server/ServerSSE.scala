package heron.server

//import java.time._
//import java.time.format.DateTimeFormatter._
import akka._
import akka.actor._
import akka.http.scaladsl._
import akka.http.scaladsl.model.sse._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.json4s.jackson.Serialization._
import scala.concurrent.duration._
import scala.io._

object ServerSSE extends App {
  implicit val system = ActorSystem("api")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val formats = org.json4s.DefaultFormats

  def fetchExamples(): Source[Array[Test], NotUsed] = Source.fromIterator(() => new BatchIterator(5, 5, 2 seconds))

  val route = {
    import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
    path("example") {
      complete{
        /*Source
          .tick(2.seconds, 2.seconds, NotUsed)
          .map(_ => LocalTime.now())
          .map(time => ServerSentEvent(ISO_LOCAL_TIME.format(time)))
          .keepAlive(1.second, () => ServerSentEvent.heartbeat)*/
        fetchExamples.map(arr => ServerSentEvent(write(arr)))
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 9090)
  println("Server started at localhost:9090")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
}
