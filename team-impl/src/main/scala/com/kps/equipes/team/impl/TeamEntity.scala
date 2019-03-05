package com.kps.equipes.team.impl

import akka.Done
import com.kps.equipes.team.api.Team
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class TeamEntity extends PersistentEntity {
 
  override type Command     = TeamCommand[_]
  override type Event       = TeamEvent
  override type State       = TeamState
  override def initialState = TeamState(None)

  override def behavior: Behavior = {
    case TeamState(None)       => newEntity
    case TeamState(Some(team)) => existingEntity
  }

  val onGetTeam = Actions().onReadOnlyCommand[GetTeam, GetTeamReply] {
    case (GetTeam(), ctx, state) => ctx.reply(GetTeamReply(state.team))
  }

  val newEntity = {
    Actions()
      .onCommand[CreateTeam, Done] {
        case (CreateTeam(team), ctx, state) =>
          ctx.thenPersist(TeamCreated(team))( _ => ctx.reply(Done))
      }
      .onCommand[ChangeTeamName, Done] {
        case (ChangeTeamName(name), ctx, state ) =>
          ctx.invalidCommand(s"Team $entityId is not yet created")
          ctx.done
      }
      .onCommand[ChangeTeamStatus, Done] {
        case (ChangeTeamStatus(active), ctx, state ) =>
          ctx.invalidCommand(s"Team $entityId is not yet created")
          ctx.done
      }
      .onEvent {
        case (TeamCreated(team), state) => TeamState(team)
      }
  }.orElse(onGetTeam)

  val existingEntity = {
    Actions()
      .onCommand[CreateTeam, Done] {
        case (CreateTeam(team), ctx, state) =>
          ctx.invalidCommand("Team already exists")
          ctx.done
      }
      .onCommand[ChangeTeamName, Done] {
        case (ChangeTeamName(newName), ctx, state) if state.team.get.name == newName =>
          ctx.reply(Done)
          ctx.done
        case (ChangeTeamName(newName), ctx, state) =>
          ctx.thenPersist(TeamNameChanged(Team(state.team.get.id, newName, state.team.get.active, state.team.get.countryId)))(_ => ctx.reply(Done))
      }
      .onCommand[ChangeTeamStatus, Done] {
        case (ChangeTeamStatus(active), ctx, state) if state.team.get.active == active =>
          ctx.reply(Done)
          ctx.done
        case (ChangeTeamStatus(active), ctx, state) =>
          ctx.thenPersist(TeamStatusChanged(Team(state.team.get.id, state.team.get.name, active, state.team.get.countryId)))(_ => ctx.reply(Done))
      }
      .onEvent {
        case (TeamNameChanged(team), state) => state.changeName(team.name)
        case (TeamStatusChanged(team), state) => state.changeStatus(team.active)
      }
  }.orElse(onGetTeam)
}
