package net.prihoda.play.casbah

import org.specs2.{ScalaCheck, Specification}
import org.scalacheck.{Gen, Arbitrary}
import org.scalacheck.Prop.forAll
import play.api.test.Helpers._
import play.api.test.FakeApplication
import com.mongodb.Mongo
import java.util.logging.{Level, Logger}
import com.mongodb.casbah.Imports._

/**
 * Created by IntelliJ IDEA.
 * User: mph
 * Date: 20.06.12
 * Time: 17:37
 */

class MongoDatabaseSpec extends Specification with ScalaCheck {
  // Turn off mongo logging for the tests
  Logger.getLogger("com.mongodb").setLevel(Level.OFF)

  def is =

    "Mongo database support should" ^ args(skipAll = !mongoRunning_?, sequential = true) ^
      "be able to connect to mongo instance" ! mongo().connect ^
      "have one connection instance per JVM" ! mongo().oneInstance ^
      "serve the 'test' db" ! mongo().testDb ^
      "serve the 'test' collection in 'test' db" ! mongo().testCollection ^
      "have a default database available" ! mongo().defaultDb ^
      "have the default database name be specifiable" ! mongo(Map(("mongo.db.uri", dbUri))).namedDb ^
      "allow to work directly with a collection" ! mongo().withCollection ^
      "the test collection should" ^
      "be empty by default" ! mongo().testEmpty ^
      "be able to save objects to" ! mongo().testSave ^
      "have the saved objects queryable by id" ^ mongo().testQuery ^ end

  val dbName = "testplugin"
  val dbUri = "mongodb://127.0.0.1/" + dbName

  val mongoRunning_? = try {
    new Mongo().getDatabaseNames
    true
  } catch {
    case _ => false
  }

  case class mongo(conf: Map[String, String] = Map.empty) {
    val db = new MongoDatabasePlugin(FakeApplication(additionalConfiguration = conf))
    db.onStart()

    // Drop test collection
    db.getDatabase().getCollection("test").drop()

    def connect = db.getConnection must not beNull

    def oneInstance = {
      implicit def arbMongo = Arbitrary {
        Gen {
          _ => Some(db.getConnection)
        }
      }
      forAll {
        (conn1: MongoConnection, conn2: MongoConnection) => conn1 === conn2
      }
    }

    def testDb = db.getConnection("test") must not beNull

    def testCollection = db.getDatabase("test")("test") must not beNull

    def defaultDb = db.getDatabase() must not beNull

    def namedDb = db.getDatabase().getName must_== dbName

    def withCollection = db.withCollection("test") { coll => coll must not beNull}

    def testEmpty = db.withCollection("test") { coll => coll.find() must beEmpty}

    def testSave = db.withCollection("test") { coll =>
      coll += MongoDBObject("key" -> "value")
      coll.find() must not beEmpty
    }

    def testQuery = db.withCollection("test") { coll =>
      val obj = MongoDBObject("key" -> "value")
      coll += obj
      coll.findOne(MongoDBObject("_id" -> obj._id.get)).get must_== obj
    }
  }

}
