package com.kps.equipes.team.impl

import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement, Row}
import com.kps.equipes.team.api.Team
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, ReadSideProcessor}
import scala.concurrent.{ExecutionContext, Future}

private[impl] class TeamRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def getTeamByName(name: String) = {
    session.selectOne(s"SELECT * FROM teams WHERE name = ?", name)
  }

  def getTeams: Future[Seq[Team]] =
    session.selectAll("SELECT * FROM teams").map(rows =>
      rows.map(row => convertTeam(row)
      )
    )

  private def convertTeam(teamRow: Row): Team =
    Team(teamRow.getUUID("id"), teamRow.getString("name"), teamRow.getBool("active"), teamRow.getUUID("countryId"))
}

private[impl] class TeamEventProcessor(session: CassandraSession,
                                       readSide: CassandraReadSide
                                      )(implicit ec: ExecutionContext) extends ReadSideProcessor[TeamEvent] {

  var insertTeamStatement: PreparedStatement = _
  var updateNameStatement: PreparedStatement = _
  var updateActiveStatement: PreparedStatement = _

  def buildHandler: ReadSideProcessor.ReadSideHandler[TeamEvent] = {
    readSide.builder[TeamEvent]("teamEventOffset")
      .setGlobalPrepare(createTables)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[TeamCreated](e => {insertTeam(e.event.team)})
      .setEventHandler[TeamNameChanged](e => {updateTeamName(e.event.team)})
      .setEventHandler[TeamStatusChanged](e => {updateTeamActive(e.event.team)})
      .build
  }

  def aggregateTags: Set[AggregateEventTag[TeamEvent]] = TeamEvent.Tags.allTags

  private def createTables() = {
    for {
      _ <- session.executeCreateTable(
        """
        CREATE TABLE IF NOT EXISTS teams(
          id UUID,
          name TEXT,
          active BOOLEAN,
          countryId UUID,
          PRIMARY KEY(id)
        )
        """)
    } yield Done
  }

  private def prepareStatements() = {
    for{
      insert       <- session.prepare("INSERT INTO teams(id, name, active, countryId) VALUES (?, ?, ?, ?)")
      updateName   <- session.prepare("UPDATE teams SET name = ? WHERE id = ?")
      updateActive <- session.prepare("UPDATE teams SET active = ? WHERE id = ?")
    } yield{
      insertTeamStatement = insert
      updateNameStatement = updateName
      updateActiveStatement = updateActive
      Done
    }
  }

  private def insertTeam(team: Team) = {
    val teamBindStatement = insertTeamStatement.bind()
    teamBindStatement.setUUID("id", team.id)
    teamBindStatement.setString("name", team.name)
    teamBindStatement.setBool("active", team.active)
    teamBindStatement.setUUID("countryId", team.countryId)
    Future.successful(List(teamBindStatement))
  }

  private def updateTeamName(team: Team) = {
    val teamBindStatement = updateNameStatement.bind()
    teamBindStatement.setUUID("id", team.id)
    teamBindStatement.setString("name", team.name)
    Future.successful(List(teamBindStatement))
  }

  private def updateTeamActive(team: Team) = {
    val teamBindStatement = updateNameStatement.bind()
    teamBindStatement.setUUID("id", team.id)
    teamBindStatement.setBool("active", team.active)
    Future.successful(List(teamBindStatement))
  }
}
