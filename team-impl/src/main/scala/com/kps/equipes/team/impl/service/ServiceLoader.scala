package com.kps.equipes.team.impl.service

import com.kps.equipes.team.api.TeamService
import com.kps.equipes.team.impl.eventsourcing.{TeamEntity, TeamProcessor, TeamRepository}
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.dns.DnsServiceLocatorComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader}
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class TeamApplicationLoader extends LagomApplicationLoader {

  override def loadDevMode(context: LagomApplicationContext) =
    new TeamApplication(context) with LagomDevModeComponents

  override def load(context: LagomApplicationContext) =
    new TeamApplication(context) with DnsServiceLocatorComponents

  override def describeService = Some(readDescriptor[TeamService])
}

abstract class TeamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[TeamService](wire[TeamServiceImpl])

  lazy val teamRepository: TeamRepository = wire[TeamRepository];

  override def jsonSerializerRegistry = TeamSerializerRegistry

  persistentEntityRegistry.register(wire[TeamEntity])

  readSide.register(wire[TeamProcessor])
}

