package com.kps.equipes.team.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import java.util.UUID
import play.api.libs.json.{Format, Json}

trait TeamService extends Service {

  /**
    * POSTs
    */
  def createTeam: ServiceCall[CreateTeamRequest, CreateTeamResponse]
  def changeTeamName(id: UUID): ServiceCall[ChangeTeamNameRequest, Done]
  def changeTeamStatus(id: UUID): ServiceCall[ChangeTeamStatusRequest, Done]

  /**
    * GETs
    */
  def getTeam(teamID: UUID): ServiceCall[NotUsed, Team]
  def getTeams: ServiceCall[NotUsed, GetTeamsResponse]
  def getTeamByName(name: String): ServiceCall[NotUsed, Team]

  override final def descriptor = {
    import Service._
    named("team-service").withCalls(
      restCall(Method.POST,"/api/teams", createTeam),
      restCall(Method.POST,"/api/teams/:id/name", changeTeamName _),
      restCall(Method.POST,"/api/teams/:id/active", changeTeamStatus _),
      restCall(Method.GET,"/api/teams/:id", getTeam _),
      restCall(Method.GET,"/api/teams", getTeams _),
      restCall(Method.GET,"/api/team/name/:name", getTeamByName _)
    ).withAutoAcl(true)
  }
}

/**
  * Requests
  */
case class CreateTeamRequest(name: String, isoCode: String)
object CreateTeamRequest {
  implicit val format: Format[CreateTeamRequest] = Json.format
}

case class ChangeTeamNameRequest(name: String)
object ChangeTeamNameRequest {
  implicit val format: Format[ChangeTeamNameRequest] = Json.format
}

case class ChangeTeamStatusRequest(active: Boolean)
object ChangeTeamStatusRequest {
  implicit val format: Format[ChangeTeamStatusRequest] = Json.format
}

/**
  * Responses
  */
case class CreateTeamResponse(id: UUID)
object CreateTeamResponse {
  implicit val format: Format[CreateTeamResponse] = Json.format
}

case class GetTeamsResponse(teams: Seq[Team])
object GetTeamsResponse {
  implicit val format: Format[GetTeamsResponse] = Json.format
}
