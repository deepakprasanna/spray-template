package com.example

import akka.actor.Actor

case object UserExists
case object RegisterUser
case object RegisteredUser
class UserExistsException extends Exception

class EitherActor extends Actor {
  def receive = {
    case RegisterUser => sender ! "Success"
    case RegisteredUser => sender ! UserExists
    case _ => println("There was some error")
  }
}
