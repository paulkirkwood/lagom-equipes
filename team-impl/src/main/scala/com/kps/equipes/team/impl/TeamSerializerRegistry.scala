package com.kps.equipes.team.impl
      
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializerRegistry, JsonSerializer}

object TeamSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(

    /**
      * State
      */
    JsonSerializer[TeamState],

    /**
      * Commands
      */
    JsonSerializer[CreateTeam],
    JsonSerializer[GetTeam],
    JsonSerializer[ChangeTeamName],
    JsonSerializer[ChangeTeamStatus],

    /**
      * Replies
      */
    JsonSerializer[GetTeamReply],

    /**
      * Events
      */
    JsonSerializer[TeamCreated],
    JsonSerializer[TeamNameChanged],
    JsonSerializer[TeamStatusChanged]
  )
}
