package com.kps.equipes.team.impl.eventsourcing

import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement, Row}
import com.kps.equipes.team.api.Team
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import scala.concurrent.{ExecutionContext, Future}

class TeamRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  var insertTeamStatement: PreparedStatement = _
  var updateNameStatement: PreparedStatement = _
  var updateActiveStatement: PreparedStatement = _

  def createTable(): Future[Done] = {
    session.executeCreateTable(
      """
        |CREATE TABLE IF NOT EXISTS teams(
        |id UUID PRIMARY KEY,
        |name TEXT,
        |active BOOLEAN
        |);
      """.stripMargin)

    session.executeCreateTable(
      """
        |CREATE INDEX IF NOT EXISTS
        |nameIndex ON teams (name);
      """.stripMargin)
  }

  def createPreparedStatements: Future[Done] = {
    for{
      insert       <- session.prepare("INSERT INTO teams(id, name, active) VALUES (?, ?, ?)")
      updateName   <- session.prepare("UPDATE teams SET name = ? WHERE id = ?")
      updateActive <- session.prepare("UPDATE teams SET active = ? WHERE id = ?")
    } yield{
      insertTeamStatement = insert
      updateNameStatement = updateName
      updateActiveStatement = updateActive
      Done
    }
  }

  def insertTeam(team: Team): Future[List[BoundStatement]] = {
    val teamBindStatement = insertTeamStatement.bind()
    teamBindStatement.setUUID("id", team.id)
    teamBindStatement.setString("name", team.name)
    teamBindStatement.setBool("active", team.active)
    Future.successful(List(teamBindStatement))
  }

  def updateTeamName(team: Team): Future[List[BoundStatement]] = {
    val teamBindStatement = updateNameStatement.bind()
    teamBindStatement.setUUID("id", team.id)
    teamBindStatement.setString("name", team.name)
    Future.successful(List(teamBindStatement))
  }

  def updateTeamActive(team: Team): Future[List[BoundStatement]] = {
    val teamBindStatement = updateNameStatement.bind()
    teamBindStatement.setUUID("id", team.id)
    teamBindStatement.setBool("active", team.active)
    Future.successful(List(teamBindStatement))
  }

  def getTeamByName(name: String): Future[Option[Team]] =
    session.selectOne(s"SELECT * FROM teams WHERE name = '$name'").map{optRow =>
      optRow.map{row => convertTeam(row)
      }
    }

  def getTeams: Future[Seq[Team]] =
    session.selectAll("SELECT * FROM teams").map(rows =>
      rows.map(row => convertTeam(row)
      )
    )

  private def convertTeam(teamRow: Row): Team =
    Team(teamRow.getUUID("id"), teamRow.getString("name"), teamRow.getBool("active"))
}
