package com.kps.equipes.country.impl

import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, ReadSideProcessor} 
import scala.concurrent.ExecutionContext
      
class CountryProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[CountryEvent] {
      
  override def aggregateTags: Set[AggregateEventTag[CountryEvent]] =  CountryEvent.Tags.allTags
    
  val countryRepository = new CountryRepository(session)

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[CountryEvent] = {
    readSide.builder[CountryEvent]("countryEventOffset")
      .setGlobalPrepare(countryRepository.createTable)
      .setPrepare(_ => countryRepository.createPreparedStatements)
      .setEventHandler[CountryCreated](e ⇒ countryRepository.insertCountry(e.event.country))
      .build()
  }
}
