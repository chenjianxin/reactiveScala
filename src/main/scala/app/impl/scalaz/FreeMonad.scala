package app.impl.scalaz

import scalaz.Free._
import scalaz.{Free, ~>}


/**
  * Created by pabloperezgarcia on 12/03/2017.
  *
  *
  */
object FreeMonad extends App {

  /**
    * With type we define a new class type instead have to create an object
    * class as we have to do in Java
    */
  type Id[+X] = X
  type Symbol = String
  type Response = Any
  type Pair = (String, Int)

  /**
    * The next classes are our ADT algebraic data types
    */

  sealed trait Orders[A]

  case class ListStocks() extends Orders[List[Symbol]]

  case class Buy(stock: Symbol, amount: Int) extends Orders[Response]

  case class Sell(stock: Symbol, amount: Int) extends Orders[Response]

  /**
    * A Free monad it´s kind like an Observable,
    * where we specify the Entry type Orders, and output type A
    */
  type OrdersFree[A] = Free[Orders, A]

  def listStocks(): OrdersFree[List[Symbol]] = {
    liftF[Orders, List[Symbol]](ListStocks())
  }

  /**
    * With liftF we specify to introduce a function into the Free Monad
    */
  def buyStock(stock: Symbol, amount: Int): OrdersFree[Response] = {
    liftF[Orders, Response](Buy(stock, amount))
  }

  def sellStock(stock: Symbol, amount: Int): OrdersFree[Response] = {
    liftF[Orders, Response](Sell(stock, amount))
  }


  val freeMonad =
    listStocks()
      .flatMap(symbols => {
        var value = ""
        var amount = 100
        try {
          value = symbols
            .filter(symbol => symbol.eq("FB"))
            .head
        } catch {
          case e: Exception =>
            value = s"ERROR $e"
            amount = 0
        }
        buyStock(value, amount)
      })
      .flatMap(pair => {
        sellStock(s"GOOG ${pair.asInstanceOf[Pair]._1}", pair.asInstanceOf[Pair]._2 + 100)
      })

//  (for {
//    stocks <- listStocks()
//    response <- buyStock(stocks.head, 1)
//    _ <- sellStock(null, 1)
//  } yield ()).run
//
//  implicit class customFree(free:Free[FreeMonad.Orders,Unit]){
//
//    def run() = free.foldMap(orderInterpreter1)
//
//  }

  /**
    * This function return a function which receive an Order type of A and return that type
    * That type it could be anything, so using the same FreeMonad DSL we can define multiple
    * implementations types.
    *
    * @return
    */
  def orderInterpreter1: Orders ~> Id = new (Orders ~> Id) {
    def apply[A](order: Orders[A]): Id[A] = order match {
      case ListStocks() =>
        println(s"Getting list of stocks: FB, TWTR")
        List("FB", "TWTR")
      case Buy(stock, amount) =>
        println(s"Buying $amount of $stock")
        new Pair(stock, amount)
      case Sell(stock, amount) =>
        println(s"Selling $amount of $stock")
        "done interpreter 1"
    }
  }

  /**
    * Thanks to the free monads defined, now using another interpreter we can create a corner case
    * to see how our monad behave for instances against a NullPointerException
    *
    * @return
    */
  def orderInterpreter2: Orders ~> Id = new (Orders ~> Id) {
    def apply[A](order: Orders[A]): Id[A] = order match {
      case ListStocks() =>
        println(s"Getting list of stocks: FB, TWTR")
        null
      case Buy(stock, amount) =>
        println(s"Buying $amount of $stock")
        new Pair(stock, amount)
      case Sell(stock, amount) =>
        println(s"Selling $amount of $stock")
        "done interpreter 2"
    }
  }

  def orderInterpreter3: Orders ~> Id = new (Orders ~> Id) {
    def apply[A](order: Orders[A]): Id[A] = order match {
      case ListStocks() =>
        println(s"Getting list of stocks: FB, TWTR")
        List("TWTR")
      case Buy(stock, amount) =>
        println(s"Buying $amount of $stock")
        new Pair(stock, amount)
      case Sell(stock, amount) =>
        println(s"Selling $amount of $stock")
        "done interpreter 3"
    }
  }


  /**
    * foldMap operator will receive a transformation function
    * which will receive the items of the pipeline monad, and it will introduce
    * the bussiness logic over the items.
    * Also, since we define a generic type for the return, this one it could be anything.
    *
    **/
  freeMonad.foldMap(orderInterpreter1)
  println("###############################")
  freeMonad.foldMap(orderInterpreter2)
  println("###############################")
  freeMonad.foldMap(orderInterpreter3)


}
