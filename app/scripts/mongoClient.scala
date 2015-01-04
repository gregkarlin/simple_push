package scripts

/**
 * Created by gregkarlin on 12/30/14.
 */


import reactivemongo.bson._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.Logger

object MongoClient {
  import scripts.migrate.MigrationActor._

  case class Profile(device_id: String, platform: String, entities: List[TopicHashEntity])

  def connect() = {
    import reactivemongo.api._


    // gets an instance of the driver
    // (creates an actor system)
    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))

    // Gets a reference to the database "plugin"
    val db = connection("migrations")

    // Gets a reference to the collection "acoll"
    // By default, you get a BSONCollection.
    val collection = db("profiles")

    collection
  }

  val collection = connect()

  implicit def toDoc(profile: Profile): BSONDocument = {
    val entities = Json.toJson(profile.entities).toString
    BSONDocument("device_id" -> profile.device_id, "platform" -> profile.platform, "entities" -> entities)
  }

  def insert(profile: BSONDocument) {
    Logger.info("inserting new profile")
    collection.insert(profile)
  }


}
