package com.kps.equipes.country.impl.eventsourcing

import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement, Row}
import com.kps.equipes.country.api.Country
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import scala.concurrent.{ExecutionContext, Future}

class CountryRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  var insertCountryStatement: PreparedStatement = _

  def createTable(): Future[Done] = {
    session.executeCreateTable(
      """
        |CREATE TABLE IF NOT EXISTS countries(
        |id UUID PRIMARY KEY,
        |name TEXT,
        |isoCode TEXT
        |);
      """.stripMargin)

    session.executeCreateTable(
      """
        |CREATE INDEX IF NOT EXISTS
        |countryNameIndex ON countries (name);
      """.stripMargin)

    session.executeCreateTable(
      """
        |CREATE INDEX IF NOT EXISTS
        |countryISOCodeIndex ON countries (isoCode);
      """.stripMargin)
  }

  def createPreparedStatements: Future[Done] = {
    for{
      insert <- session.prepare("INSERT INTO countries(id, name, isoCode) VALUES (?, ?, ?)")
    } yield{
      insertCountryStatement = insert
      Done
    }
  }

  def insertCountry(country: Country): Future[List[BoundStatement]] = {
    val countryBindStatement = insertCountryStatement.bind()
    countryBindStatement.setUUID("id", country.id)
    countryBindStatement.setString("name", country.name)
    countryBindStatement.setString("isoCode", country.isoCode)
    Future.successful(List(countryBindStatement))
  }

  def getCountryByName(name: String): Future[Option[Country]] =
    session.selectOne(s"SELECT * FROM countries WHERE name = '$name'").map{optRow =>
      optRow.map{row => convertCountry(row)
      }
    }

  def getCountryByISOCode(isoCode: String): Future[Option[Country]] =
    session.selectOne(s"SELECT * FROM countries WHERE isoCode = '$isoCode'").map{optRow =>
      optRow.map{row => convertCountry(row)
      }
    }

  def getCountries: Future[Seq[Country]] =
    session.selectAll("SELECT * FROM countries").map(rows =>
      rows.map(row => convertCountry(row)
      )
    )

  private def convertCountry(teamRow: Row): Country =
    Country(teamRow.getUUID("id"), teamRow.getString("name"), teamRow.getString("isoCode"))
}
