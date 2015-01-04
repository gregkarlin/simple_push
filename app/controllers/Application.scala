package controllers

import play.api._
import play.api.mvc._
import models.PreferenceRequest._
import akka.actor.{ActorSystem, Props}
import actors.SimplePushActor
import akka.routing.FromConfig

object Application extends Controller {

  val system = ActorSystem()

  val client = system.actorOf(FromConfig.props(Props[SimplePushActor]), "client")

  val echo = Action(parse.json) { implicit request =>
    val payload = request.body.as[PreferenceRequest]
    client ! payload
    Ok("Processing Request")
  }

}
