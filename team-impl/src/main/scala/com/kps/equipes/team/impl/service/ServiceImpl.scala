package com.kps.equipes.team.impl.service

import akka.{Done, NotUsed}
import com.kps.equipes.team.api.{CreateTeamRequest,
                                 CreateTeamResponse,
                                 ChangeTeamNameRequest,
                                 ChangeTeamStatusRequest,
                                 GetTeamsResponse,
                                 Team,
                                 TeamService}
import com.kps.equipes.team.impl.eventsourcing._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, NotFound}
import com.lightbend.lagom.scaladsl.persistence.{PersistentEntityRef, PersistentEntityRegistry}
import java.util.UUID
import scala.concurrent.ExecutionContext

class TeamServiceImpl(teamRepository: TeamRepository,
                      persistentEntityRegistry: PersistentEntityRegistry
                     )(implicit ec: ExecutionContext) extends TeamService{

  private def teamEntityRef(teamId: UUID): PersistentEntityRef[TeamCommand[_]] =
      persistentEntityRegistry.refFor[TeamEntity](teamId.toString)

  override def createTeam: ServiceCall[CreateTeamRequest, CreateTeamResponse] =
    ServiceCall { req =>
      val id = UUID.randomUUID()
      teamEntityRef(id).ask(CreateTeam(Team(id, req.name, true))).map { _ => CreateTeamResponse(id) }
    }

  override def getTeam(teamId: UUID): ServiceCall[NotUsed, Team] =
    ServiceCall { _ =>
      teamEntityRef(teamId).ask(GetTeam()).map(_.team.getOrElse(throw NotFound(s"team $teamId not found")))
    }

  override def changeTeamName(id: UUID): ServiceCall[ChangeTeamNameRequest, Done] =
    ServiceCall { req =>
      if (req.name.isEmpty) throw BadRequest("Team name cannot be empty!")
      else
        teamEntityRef(id).ask(ChangeTeamName(req.name))
    }
 
  override def changeTeamStatus(id: UUID): ServiceCall[ChangeTeamStatusRequest, Done] =
    ServiceCall { req =>
      teamEntityRef(id).ask(ChangeTeamStatus(req.active))
    }

  override def getTeams: ServiceCall[NotUsed, GetTeamsResponse] =
    ServiceCall { _ =>
      teamRepository.getTeams.map(teams => GetTeamsResponse(teams))
    }

  override def getTeamByName(name: String): ServiceCall[NotUsed, Team] =
    ServiceCall { _ =>
      teamRepository.getTeamByName(name).map(team => Team(team.get.id, team.get.name, team.get.active))
    }
}