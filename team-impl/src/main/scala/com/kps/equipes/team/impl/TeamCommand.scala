package com.kps.equipes.team.impl

import akka.Done
import com.kps.equipes.team.api.Team
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json._

/**
  * Commands
  */
sealed trait TeamCommand[R] extends ReplyType[R]

case class CreateTeam(team: Team) extends TeamCommand[Done]
object CreateTeam {
  implicit val format: Format[CreateTeam] = Json.format[CreateTeam]
}

case class GetTeam() extends TeamCommand[GetTeamReply]

object GetTeam {
  implicit val strictReads = Reads[GetTeam](json => json.validate[JsObject].filter(_.values.isEmpty).map(_ => GetTeam()))
  implicit val writes = OWrites[GetTeam](_ => Json.obj())
}

case class ChangeTeamName(name: String) extends TeamCommand[Done]

object ChangeTeamName {
  implicit val format: Format[ChangeTeamName] = Json.format[ChangeTeamName]
}

case class ChangeTeamStatus(active: Boolean) extends TeamCommand[Done]

object ChangeTeamStatus {
  implicit val format: Format[ChangeTeamStatus] = Json.format[ChangeTeamStatus]
}

/**
  * Replies
  */
case class GetTeamReply(team: Option[Team])

object GetTeamReply {
  implicit val format: Format[GetTeamReply] = Json.format[GetTeamReply]
}
