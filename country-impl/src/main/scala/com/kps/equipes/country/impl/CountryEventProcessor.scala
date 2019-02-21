package com.kps.equipes.country.impl

import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import scala.concurrent.{ExecutionContext, Future, Promise}
      
class CountryEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[CountryEvent] {
      
  override def aggregateTags: Set[AggregateEventTag[CountryEvent]] =  CountryEvent.Tags.allTags
    
  private def createTable(): Future[Done] = {
    for {
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS countries (
        id UUID,
        name TEXT,
        isoCode TEXT,
        PRIMARY KEY (id))
      """)
      _ <- session.executeCreateTable("""
        CREATE INDEX IF NOT EXISTS countriesNameIndex ON countries (name)
      """)
      _ <- session.executeCreateTable("""
        CREATE INDEX IF NOT EXISTS countriesISOCodeIndex ON countries (isoCode)
      """)
    } yield Done
  } 

  private val insertCountryPromise = Promise[PreparedStatement]
  private def insertCountry: Future[PreparedStatement] = insertCountryPromise.future

  private def prepareCountryStatements(): Future[Done] = {
    val insert = session.prepare("INSERT INTO countries (id, name, isoCode) VALUES (?, ?, ?)")
    insertCountryPromise.completeWith(insert)
    insert.map(_ => Done)
  }

  private def processCountryCreated(eventElement: EventStreamElement[CountryCreated]): Future[List[BoundStatement]] = {
    insertCountry.map { ps =>
      val bindInsertCountry = ps.bind()
      bindInsertCountry.setUUID("id", eventElement.event.country.id)
      bindInsertCountry.setString("name", eventElement.event.country.name)
      bindInsertCountry.setString("isoCode", eventElement.event.country.isoCode)
      List(bindInsertCountry)
    }
  }

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[CountryEvent] = {
    val builder = readSide.builder[CountryEvent]("countriesoffset")
    builder.setGlobalPrepare(() => createTable())
    builder.setPrepare(tag => prepareCountryStatements())
    builder.setEventHandler[CountryCreated](processCountryCreated)
    builder.build()
  }

}
