package com.kps.equipes.team.impl.service

import com.kps.equipes.country.api.CountryService
import com.kps.equipes.team.api.TeamService
import com.kps.equipes.team.impl.eventsourcing.{TeamEntity, TeamProcessor, TeamRepository}
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class TeamServiceLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new TeamServiceApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new TeamServiceApplication(context) with LagomDevModeComponents
}

abstract class TeamServiceApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[TeamService](wire[TeamServiceImpl])

  lazy val teamRepository: TeamRepository = wire[TeamRepository];

  override def jsonSerializerRegistry = TeamSerializerRegistry

  persistentEntityRegistry.register(wire[TeamEntity])

  readSide.register(wire[TeamProcessor])

  lazy val countryService = serviceClient.implement[CountryService]
}
