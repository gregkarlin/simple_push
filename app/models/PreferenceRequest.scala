package models

import play.api.libs.json._

/**
 * Created by gregkarlin on 12/15/14.
 */
object PreferenceRequest {

  case class Subscription(
    entity_type: String,
    entity_id: Int,
    edition: String
  )

  case class PreferenceRequest(
    version: String,
    device: String,
    edition: String,
    platform: String,
    device_id: String,
    subscriptions: List[Subscription]
  )

  implicit val subscriptionFormat = Json.format[Subscription]
  implicit val preferenceRequestFormat = Json.format[PreferenceRequest]

}
