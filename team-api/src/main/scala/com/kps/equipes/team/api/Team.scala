package com.kps.equipes.team.api

import java.util.UUID
import play.api.libs.json.{Format, Json}

case class Team(id: UUID, name: String, active: Boolean, countryId: UUID)

object Team {
  implicit val format: Format[Team] = Json.format
}
