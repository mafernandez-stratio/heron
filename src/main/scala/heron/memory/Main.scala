package heron.memory

import scala.io.StdIn

case class Example(id: Int, name: String, rating: Double)

object Main extends App {

  val limit = 12345678
  val arr = Range(1, limit).map(n => Example(n, s"name_$n", n / limit)).grouped(limit / 100).toArray.map(_.toArray)
  println("Ready?")
  StdIn.readLine()
  val init = System.currentTimeMillis()
  //val result = Array.concat(arr: _*)
  val result = arr.foldLeft(Array.empty[Example])(_ ++ _)
  result.map(e => Example(e.id*2, e.name*2, e.rating*2))
  println(s"Time: ${System.currentTimeMillis() - init} ms")
  println("Finish?")
  StdIn.readLine()
}
