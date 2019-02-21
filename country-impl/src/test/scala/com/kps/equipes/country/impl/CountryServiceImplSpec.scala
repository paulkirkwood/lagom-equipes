package com.kps.equipes.country.impl

import com.kps.equipes.country.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest._
import java.util.UUID
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class CountryServiceImplSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val server = startServer(defaultSetup.withCassandra(true)) { ctx =>
    new CountryApplication(ctx) with LocalServiceLocator
  }
  
  val client = server.serviceClient.implement[CountryService]
  
  override protected def afterAll(): Unit = server.stop()

  "country-service" should {
    "create a country" in {
      for {
        countryID <- client.createCountry.invoke(CreateCountryRequest("United Kingdom", "GBR"))
        response <- client.getCountry(countryID.id).invoke()
      } yield {
        response.name should equal("United Kingdom")
        response.isoCode should equal("GBR")
      }
    }
  }
}
