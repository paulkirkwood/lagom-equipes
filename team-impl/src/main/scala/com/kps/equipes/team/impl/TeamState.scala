package com.kps.equipes.team.impl

import com.kps.equipes.team.api.Team
import java.util.UUID
import play.api.libs.json._

case class TeamState(team:Option[Team]) {

  def changeName(newName: String): TeamState = team match {
    case None       => throw new IllegalStateException("name can't be changed before team is created")
    case Some(team) => TeamState(team.copy(name = newName))
  }                      

  def changeStatus(newActive: Boolean): TeamState = team match {
    case None       => throw new IllegalStateException("status can't be changed before team is created")
    case Some(team) => TeamState(team.copy(active = newActive))
  }                      
}

object TeamState {
  implicit val format: Format[TeamState] = Json.format[TeamState]

  def apply(team: Team): TeamState = new TeamState(Some(team))
}
