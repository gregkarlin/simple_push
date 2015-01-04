package scripts.migrate

import play.api.db._
import anorm._
import play.api.Play.current
import play.api.Logger
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import akka.actor._
import akka.routing.RoundRobinPool
import scripts.MongoClient
import play.api.libs.json._
class Migrator {
  implicit val connection = DB.getConnection()

  import MigrationActor._

  def getEntities(hash: TopicHash){
    val ents: Future[List[TopicHashEntity]] = Future {
      //DB.withConnection { implicit cn =>
      val entitiesQuery = SQL(s"SELECT entity_id, edition, entity_type from topic_hash_entities where hash_id = ${hash.hashId}")
      entitiesQuery().map {
        row =>
          TopicHashEntity(row[Int]("entity_id"), row[String]("edition"), row[String]("entity_type"))
      }.toList
      //  }
    }
    ents.onComplete {
      case Success(result) =>
        //val mobileProfilesQuery = SQL(s"SELECT device_id, platform FROM mobile_profiles where topic_arn = '${rez._1}")
        hash.actor ! Entities(result, hash.topicArn, 0, hash.actor)

      case Failure(res) =>
        Logger.info("Failed for the following reasons " + res.toString)
    }
  }

  def insertProfiles(entity: Entities) {
    val offset = entity.offset
    val limit = 100
    val mobileProfilesQuery = SQL(s"SELECT device_id, platform FROM mobile_profiles where topic_arn = '${entity.topicArn}' LIMIT $limit OFFSET $offset")
    val profiles = Future {
      mobileProfilesQuery().map {
        row =>
          (row[String]("device_id"), row[String]("platform"))
      }.toList

    }
    profiles.onComplete {
      case Success(result) =>
        val newEntitities = Entities(entity.entities, entity.topicArn, entity.offset + limit, entity.actor)
        if (result.size == limit) entity.actor ! newEntitities
        result.map {
          profile =>
            val mp = MongoClient.Profile(profile._1, profile._2, entity.entities)
            MongoClient.insert(mp)
        }
      case Failure(reason) =>
        Logger.info(reason.toString)
    }

  }

}

object MigrationActor {

  case class TopicHash(hashId: Int, topicArn: String, actor: ActorRef)
  case class TopicHashEntity(entityId: Int, edition: String, entityType: String)
  case class Entities(entities: List[TopicHashEntity], topicArn: String, offset: Int, actor: ActorRef)

  implicit def entitiesJson = Json.format[TopicHashEntity]

}

class MigrationActor extends Actor {

  val migrator = new Migrator
  import MigrationActor._

  def receive = {

    case rez: ((String, Int), ActorRef) =>
      migrator.getEntities(TopicHash(rez._1._2,rez._1._1,rez._2))

    case entities : Entities=>
      Logger.info("asd")
      migrator.insertProfiles(entities)


    case offset: Int =>
      Migrate.migrate(offset)
    //case entities: (String, Int, )
  }
}


object Migrate {

  //val ds = DB.getConnection()
  new play.core.StaticApplication(new java.io.File("."))

      val system = ActorSystem()
      //implicit val connection = DB.getConnection()

      val migrators = system.actorOf(RoundRobinPool(10).props(Props[MigrationActor]), "router2")

      def migrate(offset: Int) {
        DB.withConnection {
        implicit conn =>
          val limit = 10
          val result = SQL(s"SELECT topic_arn, hash_id FROM topic_hashes LIMIT $limit OFFSET $offset")
          val results = result().map {
            row =>
              (row[String]("topic_arn"), row[Int]("hash_id"))

          }.toList
          val newOffset = offset + limit
          if (results.size == limit) migrators ! newOffset
          results.map {
            rez =>
              migrators ! (rez, migrators)
            //getEntities(rez)
          }

      }
  }
}