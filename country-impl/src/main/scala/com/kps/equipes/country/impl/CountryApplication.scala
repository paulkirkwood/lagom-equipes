package com.kps.equipes.country.impl

import com.kps.equipes.country.api.CountryService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class CountryApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new CountryServiceApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CountryServiceApplication(context) with LagomDevModeComponents
}

abstract class CountryServiceApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  // Bind to the service
  override lazy val lagomServer = serverFor[CountryService](wire[CountryServiceImpl])

  //Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: CountrySerializerRegistry.type = CountrySerializerRegistry

  // Register the lagom persistent entity
  persistentEntityRegistry.register(wire[CountryEntity])

  lazy val repository: CountryRepository = wire[CountryRepository]

  // Register the lagom persistent read side processor persistent entity
  readSide.register(wire[CountryEventProcessor])
}
