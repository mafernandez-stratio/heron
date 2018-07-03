package heron.server

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshallers.sprayjson._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.io.StdIn

case class Example(id: Long, txt: String, number: Double)

object MyJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val exampleFormat = jsonFormat3(Test)
}

class BatchIterator(batchSize: Int, numberOfBatches: Int, pause: FiniteDuration) extends Iterator[Array[Test]]{

  val range = Range(0, batchSize*numberOfBatches).toIterator
  val numberOfBatchesIter = Range(0, numberOfBatches).toIterator

  var iter = 1

  override def hasNext: Boolean = range.hasNext

  override def next(): Array[Test] = {
    println(s"Iteration $iter/$numberOfBatches")
    println(s"Sleeping for ${pause.toMillis} ms")
    Thread.sleep(pause.toMillis)
    println(s"Taking $batchSize elements")
    iter = iter+1
    Range(0, batchSize).map{ _ =>
      val count = range.next()
      Test(count, s"Text$count", count*0.5)
    }.toArray
  }
}

object Server extends App {
  import MyJsonProtocol._
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
    .withFramingRenderer(
      Flow[ByteString].intersperse(ByteString(System.lineSeparator))
    )

  implicit val system = ActorSystem("api")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def fetchExamples(): Source[Array[Test], NotUsed] = Source.fromIterator(() => new BatchIterator(5, 50, 2 seconds))

  val route =
    path("example") {
      complete(fetchExamples)
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 9090)
  println("Server started at localhost:9090")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
}
