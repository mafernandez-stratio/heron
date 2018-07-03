package heron.server

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

import scala.concurrent.duration._
import scala.io.StdIn

case class Test(id: Long, txt: String, number: Double) {
  override def toString: String = s"$id,$txt,$number"
}

class PausedIterator(batchSize: Int, numberOfBatches: Int, pause: FiniteDuration) extends Iterator[Array[Test]]{

  var iter = 1

  val range = Range(0, batchSize*numberOfBatches).toIterator
  val numberOfBatchesIter = Range(0, numberOfBatches).toIterator

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

object DummyServer extends App {
  implicit val system = ActorSystem("api")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val toResponseMarshaller: ToResponseMarshaller[Source[Array[Test], Any]] =
    Marshaller.opaque { items =>
      val data = items.map(item => ChunkStreamPart(s"${item.mkString("|")}${System.lineSeparator}"))
      HttpResponse(entity = HttpEntity.Chunked(ContentTypes.`text/plain(UTF-8)`, data))
    }

  def fetchExamples(): Source[Array[Test], NotUsed] = Source.fromIterator(() => new BatchIterator(5, 5, 2 seconds))

  val route =
    path("example") {
      complete(fetchExamples)
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 9090)
  println("Dummy Server started at localhost:9090")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
}
