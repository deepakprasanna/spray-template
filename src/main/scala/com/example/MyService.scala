package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import reflect.ClassTag
import scala.concurrent.ExecutionContext.Implicits.global
import spray.util.LoggingContext
import spray.http.StatusCodes._
import spray.routing._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {
  implicit val timeout = Timeout(2.seconds)

  implicit def myExceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler {
      case e: UserExistsException => ctx =>
        log.warning("Error encountered while handling request: {}", ctx.request)
        ctx.complete(StatusCodes.NotFound, "Handling exception with spray")
    }

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    } ~ path("success") {
      get {
        complete {
          (Boot.eitherActor ? RegisterUser).mapTo[String]
        }
      }
    } ~ path("failure") {
      get {
        complete {
          (Boot.eitherActor ? RegisteredUser).collect {
            case s:String => s
            case UserExists => throw new UserExistsException
          }
        }
      }
    }
}
