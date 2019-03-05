package com.kps.equipes.team.impl

import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, ReadSideProcessor}

import scala.concurrent.ExecutionContext

class TeamProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[TeamEvent] {

  val teamRepository = new TeamRepository(session)

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[TeamEvent] = {
    val builder = readSide.builder[TeamEvent]("teamsoffset")
    builder.setGlobalPrepare(teamRepository.createTable)
    .setPrepare(_ => teamRepository.createPreparedStatements)
    builder.setEventHandler[TeamCreated](e ⇒ teamRepository.insertTeam(e.event.team))
    builder.setEventHandler[TeamNameChanged](e ⇒ teamRepository.updateTeamName(e.event.team))
    builder.setEventHandler[TeamStatusChanged](e ⇒ teamRepository.updateTeamActive(e.event.team))
    builder.build()
  }

  override def aggregateTags: Set[AggregateEventTag[TeamEvent]] = TeamEvent.Tags.allTags
}
