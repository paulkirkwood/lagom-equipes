package com.kps.equipes.country.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.kps.equipes.country.api.Country
import com.kps.equipes.country.impl.eventsourcing._
import com.kps.equipes.country.impl.service.{CountryApplication, CountrySerializerRegistry}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.InvalidCommandException
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{PersistentEntityTestDriver, ServiceTest}
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class CountryEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  val system = ActorSystem("CountryEntitySpecSystem",
    JsonSerializerRegistry.actorSystemSetupFor(CountrySerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
 
  def withTestDriver(block: PersistentEntityTestDriver[CountryCommand[_], CountryEvent, CountryState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new CountryEntity, "test-country-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "The country entity" should {

    "not be initialized by default" in withTestDriver { driver =>
      val outcome = driver.run(GetCountry())
      outcome.replies should contain only GetCountryReply(None)
    }
  }
}
