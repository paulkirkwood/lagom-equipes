package com.kps.equipes.country.impl

import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement, Row}
import com.kps.equipes.country.api.Country
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, ReadSideProcessor}
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import scala.concurrent.{ExecutionContext, Future}

private[impl] class CountryRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def getCountryByName(name: String) = {
    session.selectOne(s"SELECT * FROM countries WHERE name = ?", name)
  }

  def getCountryByISOCode(isoCode: String) = {
    session.selectOne(s"SELECT * FROM countries WHERE isoCode = ?", isoCode)
  }

  def getCountries: Future[Seq[Country]] =
    session.selectAll("SELECT * FROM countries").map(rows =>
      rows.map(row => convertCountry(row)
      )
    )

  private def convertCountry(row: Row): Country =
    Country(row.getUUID("id"), row.getString("name"), row.getString("isoCode"))
}

private[impl] class CountryEventProcessor(session: CassandraSession,
                                          readSide: CassandraReadSide)
                                         (implicit ec: ExecutionContext) extends ReadSideProcessor[CountryEvent] {

  private var insertCountryStatement: PreparedStatement = _

  def aggregateTags: Set[AggregateEventTag[CountryEvent]] = CountryEvent.Tags.allTags

  def buildHandler: ReadSideProcessor.ReadSideHandler[CountryEvent] = {
    readSide.builder[CountryEvent]("countryEventOffset")
      .setGlobalPrepare(createTables)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[CountryCreated](e => {insertCountry(e.event.country)})
      .build
  }

  private def createTables() = {
    for {
      _ <- session.executeCreateTable(
        """
          CREATE TABLE IF NOT EXISTS countries(
            id UUID,
            name TEXT,
            isoCode TEXT,
            PRIMARY KEY(id)
          )
        """)
      _ <- session.executeCreateTable("CREATE INDEX IF NOT EXISTS countryNamesIndex ON countries (name)")
      _ <- session.executeCreateTable("CREATE INDEX IF NOT EXISTS countryISOCodesIndex ON countries (isoCode)")
    } yield Done
  }

  private def prepareStatements() = {  
    for{
      insert <- session.prepare("INSERT INTO countries(id, name, isoCode) VALUES (?, ?, ?)")
    } yield{
      insertCountryStatement = insert
      Done
    }
  }

  private def insertCountry(country: Country) = {
    val countryBindStatement = insertCountryStatement.bind()
    countryBindStatement.setUUID("id", country.id)
    countryBindStatement.setString("name", country.name)
    countryBindStatement.setString("isoCode", country.isoCode)
    Future.successful(List(countryBindStatement))
  }

}
