package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

/** A Compilation Node.
  *
  * @constructor for the servers which run BCS
  * @param id the database-given ID of the node
  * @param hostname the server's full hostname
  * @param arch the server's process architecture
  * @param enabled determins whether or not requests are sent to this node
  * @todo implement methods to get and store statistics about a compilation
  */
case class Server(
  id: Int,
  hostname: String,
  arch: String,
  enabled: Boolean) {
}

/** Construct an instance of a Server. */
object Server {
  /** Represent a single server. */
  val simple = {
    get[Int]("id") ~
    get[String]("hostname") ~
    get[String]("arch") ~
    get[Boolean]("enabled") map {
      case id ~ hostname ~ arch ~ enabled => Server(id, hostname, arch, enabled)
    }
  }

  /** Fetch a node by its ID.
    *
    * @param id the database-given ID for the server
    * @return an Option[Server] depending on whether or not a valid server was
    *         found
    */
  def getByID(id: Long): Option[Server] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM servers WHERE id={id}").on(
      'id -> id
    ).as(Server.simple.singleOpt)
  }

  /** Fetch all nodes in the database.
    *
    * @return a List[Server] which contains every server contained in the
    *         database.
    */
  def getAllServers(): List[Server] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM servers").as(Server.simple *)
  }
}
