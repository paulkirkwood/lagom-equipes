package com.kps.equipes.country.impl

import com.kps.equipes.country.api.CountryService
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.dns.DnsServiceLocatorComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader}
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class CountryLoader extends LagomApplicationLoader {

  override def loadDevMode(context: LagomApplicationContext) =
    new CountryApplication(context) with LagomDevModeComponents

  override def load(context: LagomApplicationContext) =
    new CountryApplication(context) with DnsServiceLocatorComponents

  override def describeService = Some(readDescriptor[CountryService])
}

abstract class CountryApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[CountryService](wire[CountryServiceImpl])

  override def jsonSerializerRegistry = CountrySerializerRegistry

  persistentEntityRegistry.register(wire[CountryEntity])

  readSide.register(wire[CountryEventProcessor])
}
