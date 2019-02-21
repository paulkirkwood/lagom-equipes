package com.kps.equipes.team.impl

import com.kps.equipes.team.api.Team
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag}
import play.api.libs.json.{Format, Json}

object TeamEvent {
  val Tags = AggregateEventTag.sharded[TeamEvent](3)
}

sealed trait TeamEvent extends AggregateEvent[TeamEvent] {
  override def aggregateTag: AggregateEventShards[TeamEvent] = TeamEvent.Tags
}

case class TeamCreated(team: Team) extends TeamEvent

object TeamCreated {
  implicit val format: Format[TeamCreated] = Json.format[TeamCreated]
}   

case class TeamNameChanged(team: Team) extends TeamEvent

object TeamNameChanged {
  implicit val format: Format[TeamNameChanged] = Json.format[TeamNameChanged]
}   

case class TeamStatusChanged(team: Team) extends TeamEvent

object TeamStatusChanged {
  implicit val format: Format[TeamStatusChanged] = Json.format[TeamStatusChanged]
}
