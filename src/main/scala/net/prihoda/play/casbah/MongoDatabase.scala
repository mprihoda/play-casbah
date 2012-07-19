package net.prihoda.play.casbah

import com.mongodb.casbah.{MongoURI, MongoDB, MongoCollection, MongoConnection}
import play.api.{Logger, Configuration, Application, Plugin}
import scala.util.control.Exception._

/**
 * Singleton facade for the MongoDatabasePlugin.
 */
object MongoDatabase {

  // Error to throw on missing plugin
  private def error = throw new Exception("Mongo database plugin not registered.")

  def available_?(implicit app: Application) = app.plugin[MongoDatabasePlugin].flatMap(p =>
    allCatch opt (p.getConnection.getDatabaseNames() != null)).getOrElse(false)

  def getConnection(implicit app: Application) = app.plugin[MongoDatabasePlugin].map(_.getConnection).getOrElse(error)

  def getDatabase(name: Option[String] = None)(implicit app: Application): MongoDB =
    app.plugin[MongoDatabasePlugin].map(_.getDatabase(name)).getOrElse(error)

  def getDatabase(name: String)(implicit app: Application): MongoDB =
    app.plugin[MongoDatabasePlugin].map(_.getDatabase(name)).getOrElse(error)

  def withCollection[T](name: String)(block: MongoCollection => T)(implicit app: Application): T =
    app.plugin[MongoDatabasePlugin].map(_.withCollection(name)(block)).getOrElse(error)

}

/**
 * MongoDatabasePlugin operations.
 */
class MongoDatabasePlugin(app: Application) extends Plugin {

  // There should be only one mongo connection per JVM / DB
  private[casbah] var _conn: MongoConnection = _

  private val conf = app.configuration.getConfig("mongo").getOrElse(Configuration.empty)

  private val dbURI = MongoURI(conf.getString("db.uri").getOrElse("mongodb://127.0.0.1/test"))

  /**
   * Return the default connection
   *
   * @return Default Mongo connection
   */
  def getConnection = _conn

  /**
   * Get a MongoDatabasePlugin from the default connection by name.
   *
   * @param name Desired database name
   * @return The MongoDatabasePlugin object for given name
   */
  def getDatabase(name: String): MongoDB = getConnection.getDB(name)

  /**
   * Get a default MongoDatabasePlugin from the default connection.
   *
   * Specified by configuration mongo.default.name or "test"
   *
   * @return The default MongoDatabasePlugin
   */
  def getDatabase(name: Option[String] = None): MongoDB = getDatabase(name.getOrElse(dbURI.database))

  /**
   * Perform an operation on a collection from default database.
   *
   * @param name The collection name
   * @param block The operation to perform
   * @return The operation result
   */
  def withCollection[T](name: String)(block: MongoCollection => T): T = block(getDatabase()(name))

  override def onStart() {
    Logger.debug("Connecting to mongodb @ " + dbURI)
    _conn = MongoConnection(dbURI)
    // Authenticate to the default database, if needed
    for {
      database <- Option(dbURI.database)
      username <- Option(dbURI.username)
      password <- Option(dbURI.password)
    } _conn.getDB(database).authenticate(username, password.mkString)
  }

  override def onStop() {
    Option(_conn).map(_.close())
    _conn = null
  }
}
