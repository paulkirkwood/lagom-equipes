package com.kps.equipes.team.api

import java.util.UUID
import play.api.libs.json.{Format, Json}

case class Team(id: UUID, name: String, active: Boolean)

object Team {
  implicit val format: Format[Team] = Json.format

  def apply(name: String): Team = Team(UUID.randomUUID(), name, true)
}
