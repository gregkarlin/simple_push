package actors

import models.PreferenceRequest._
import akka.actor.Actor
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.ws._
import play.api.Logger



object SimplePushClient {
  val url = current.configuration.getString("simplepush.url").get
  val appId = current.configuration.getString("simplepush.appId").get
}

class SimplePushClient {
  import SimplePushClient._

  private def formTag(subscription: Subscription): String = {
    s"${subscription.edition}-${subscription.entity_type}-${subscription.entity_id}"
  }

  private def getTags(subscriptions: List[Subscription]) = {
    var tags = List[String]()
    subscriptions.map ( subscription => tags ++= List(formTag(subscription)))
    Json.toJson(tags)
  }

  private def getNet(platform: String) = {
    platform match {
      case "apns" => "APNs"
      case "gcm" => "GCM"
    }
  }

  def subscribe( req: PreferenceRequest ){

    val tags = getTags(req.subscriptions)

    val request = Json.obj(
      "appId" -> appId,
      "token" -> req.device_id,
      "net" -> getNet(req.platform),
      "uuid" -> java.util.UUID.randomUUID.toString,
      "tags" -> tags
    )
    Logger.info("Registering Device with SimplePush")
    Logger.info(s"request: $request")
    WS.url(url).post(request)
  }


}

class SimplePushActor extends Actor {

  val client = new SimplePushClient

  def receive = {

    case req: PreferenceRequest =>
      client.subscribe(req)

  }
}
