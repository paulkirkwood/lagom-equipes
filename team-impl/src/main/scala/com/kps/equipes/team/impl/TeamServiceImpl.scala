package com.kps.equipes.team.impl

import java.util.UUID
import akka.Done
import com.kps.equipes.team.api
import com.kps.equipes.team.api.TeamService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, NotFound}
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.lightbend.lagom.scaladsl.persistence.{PersistentEntityRef, PersistentEntityRegistry}
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.collection.immutable.Seq

class TeamServiceImpl(persistentEntityRegistry: PersistentEntityRegistry,
                      session: CassandraSession
                     )(implicit ec: ExecutionContext) extends TeamService{

  private def teamEntityRef(teamId: UUID): PersistentEntityRef[TeamCommand[_]] =
      persistentEntityRegistry.refFor[TeamEntity](teamId.toString)

  override def createTeam = ServiceCall { req =>
    session.selectOne("SELECT id FROM teams where name = ?", req.name).map {
      case Some(row) => throw BadRequest(s"Team '${req.name}' already exists")
      case None =>
    }
    val id = UUID.randomUUID()
    teamEntityRef(id).ask(CreateTeam(api.Team(id, req.name, true))).map { _ => api.TeamID(id) }
  }

  override def getTeam(teamId: UUID) = ServiceCall { req =>
    teamEntityRef(teamId).ask(GetTeam()).map(_.team.getOrElse(throw NotFound(s"team $teamId not found")))
  }

  override def changeTeamName(id: UUID) = ServiceCall { req =>
    if (req.name.isEmpty) throw BadRequest("Team name cannot be empty!")
    else
      teamEntityRef(id).ask(ChangeTeamName(req.name))
  }
 
  override def changeTeamStatus(id: UUID) = ServiceCall { req =>
    teamEntityRef(id).ask(ChangeTeamStatus(req.active))
  }

  //private val DefaultPageSize = 10
  //override def getTeams( pageNo: Option[Int], pageSize: Option[Int]) = { req =>
  //  session.select("SELECT id,name,active FROM teams LIMIT ?",Integer.valueOf(pageSize.getOrElse(DefaultPageSize))).map { row =>
  //    api.Team(
  //      row.getUUID("id"),
  //      row.getString("name"),
  //      row.getBool("active")
  //    )
  //  }.runFold(Seq.empty[api.Team])((acc, e) => acc :+ e)
  //}
}
