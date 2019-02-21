package com.kps.equipes.team.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import java.util.UUID
import play.api.libs.json.{Format, Json}
import scala.collection.immutable.Seq

trait TeamService extends Service {

  /**
    * POSTs
    */
  def createTeam: ServiceCall[CreateTeamRequest, TeamID]

  /**
    * GETs
    */
  def getTeam(teamID: UUID): ServiceCall[NotUsed, Team]
  //def getTeams(pageNo: Option[Int], pageSize: Option[Int]): ServiceCall[NotUsed,Seq[Team]]

  def changeTeamName(id: UUID): ServiceCall[ChangeTeamNameRequest, Done]
  def changeTeamStatus(id: UUID): ServiceCall[ChangeTeamStatusRequest, Done]

  override final def descriptor = {
    import Service._
    named("team-service").withCalls(
      restCall(Method.POST,"/api/teams", createTeam),
      restCall(Method.GET,"/api/teams/:id", getTeam _),
      restCall(Method.PATCH,"/api/teams/:id", changeTeamName _),
      restCall(Method.PATCH,"/api/teams/:id", changeTeamStatus _)
      //restCall(Method.GET,"/api/teams?pageNo&pageSize", getTeams _)
      //restCall(Method.PUT,"/api/teams/:id/name", changeTeamName _),
    ).withAutoAcl(true)
  }
}

case class CreateTeamRequest(name: String)
object CreateTeamRequest {
  implicit val format: Format[CreateTeamRequest] = Json.format
}

case class TeamID(id: UUID)
object TeamID {
  implicit val format: Format[TeamID] = Json.format
}

case class Team(id: UUID, name: String, active: Boolean)
object Team {
  implicit val format: Format[Team] = Json.format

  def apply(name: String): Team = Team(UUID.randomUUID(), name, true)
}

case class ChangeTeamNameRequest(name: String)
object ChangeTeamNameRequest {
  implicit val format: Format[ChangeTeamNameRequest] = Json.format
}

case class ChangeTeamStatusRequest(active: Boolean)
object ChangeTeamStatusRequest {
  implicit val format: Format[ChangeTeamStatusRequest] = Json.format
}
