package com.kps.equipes.team.impl

import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import scala.concurrent.{ExecutionContext, Future, Promise}
    
class TeamEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[TeamEvent] {

  override def aggregateTags: Set[AggregateEventTag[TeamEvent]] =  TeamEvent.Tags.allTags

  private def createTable(): Future[Done] = {
    for {
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS teams (
          id UUID,
          name TEXT,
          active BOOLEAN,
          PRIMARY KEY (id)
        )
      """)
      _ <- session.executeCreateTable("""
        CREATE INDEX IF NOT EXISTS teamsNameIndex ON teams (name)
      """)
    } yield Done
  }

  private val insertTeamPromise = Promise[PreparedStatement]
  private def insertTeam: Future[PreparedStatement] = insertTeamPromise.future

  private val updateTeamNamePromise = Promise[PreparedStatement]
  private def updateTeamName: Future[PreparedStatement] = updateTeamNamePromise.future

  private val updateTeamStatusPromise = Promise[PreparedStatement]
  private def updateTeamStatus: Future[PreparedStatement] = updateTeamStatusPromise.future

  private def prepareTeamStatements(): Future[Done] = {
    val insert = session.prepare("INSERT INTO teams (id, name, active) VALUES (?, ?, ?)")
    insertTeamPromise.completeWith(insert)
    insert.map(_ => Done)
    val updateName = session.prepare("UPDATE teams SET name = ? where id = ?")
    updateTeamNamePromise.completeWith(updateName)
    updateName.map(_ => Done)
    val updateStatus = session.prepare("UPDATE teams SET active = ? where id = ?")
    updateTeamStatusPromise.completeWith(updateStatus)
    updateStatus.map(_ => Done)
  }

  private def processTeamCreated(eventElement: EventStreamElement[TeamCreated]): Future[List[BoundStatement]] = {
    insertTeam.map { ps =>
      val bindInsertTeam = ps.bind()
      bindInsertTeam.setUUID("id", eventElement.event.team.id)
      bindInsertTeam.setString("name", eventElement.event.team.name)
      bindInsertTeam.setBool("active", eventElement.event.team.active)
      List(bindInsertTeam)
    }
  }

  private def processTeamNameChanged(eventElement: EventStreamElement[TeamNameChanged]): Future[List[BoundStatement]] = {
    updateTeamName.map { ps =>
      val bindUpdateTeamName = ps.bind()
      bindUpdateTeamName.setUUID("id", eventElement.event.team.id)
      bindUpdateTeamName.setString("name", eventElement.event.team.name)
      List(bindUpdateTeamName)
    }
  }

  private def processTeamStatusChanged(eventElement: EventStreamElement[TeamStatusChanged]): Future[List[BoundStatement]] = {
    updateTeamStatus.map { ps =>
      val bindUpdateTeamStatus = ps.bind()
      bindUpdateTeamStatus.setUUID("id", eventElement.event.team.id)
      bindUpdateTeamStatus.setBool("active", eventElement.event.team.active)
      List(bindUpdateTeamStatus)
    }
  }
  override def buildHandler(): ReadSideProcessor.ReadSideHandler[TeamEvent] = {
    val builder = readSide.builder[TeamEvent]("teamsoffset")
    builder.setGlobalPrepare(() => createTable())
    builder.setPrepare(tag => prepareTeamStatements())
    builder.setEventHandler[TeamCreated](processTeamCreated)
    builder.setEventHandler[TeamNameChanged](processTeamNameChanged)
    builder.setEventHandler[TeamStatusChanged](processTeamStatusChanged)
    builder.build()
  }
}
