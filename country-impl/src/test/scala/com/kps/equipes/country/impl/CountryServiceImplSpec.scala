package com.kps.equipes.country.impl

import com.kps.equipes.country.api.{CountryService,
                                    CreateCountryRequest}
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest._
import java.util.UUID
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}

class CountryServiceImplSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val server = startServer(defaultSetup.withCassandra(true)) { ctx =>
    new CountryServiceApplication(ctx) with LocalServiceLocator
  }
  
  val countryService: CountryService = server.serviceClient.implement[CountryService]
  
  override protected def afterAll(): Unit = server.stop()

  "country-service" should {
    "create a country" in {
      for {
        countryID <- countryService.createCountry.invoke(CreateCountryRequest("United Kingdom", "GBR"))
        response <- countryService.getCountry(countryID.id).invoke()
      } yield {
        response.name should equal("United Kingdom")
        response.isoCode should equal("GBR")
      }
    }

    "get all the countries" in {
      (for {
        belgiumID <- countryService.createCountry.invoke(CreateCountryRequest("Belgium", "BEL"))
        franceID  <- countryService.createCountry.invoke(CreateCountryRequest("France", "FRA"))
        italyID   <- countryService.createCountry.invoke(CreateCountryRequest("Italy", "ITA"))
        belgium   <- countryService.getCountry(belgiumID.id).invoke()
        france    <- countryService.getCountry(franceID.id).invoke()
        italy     <- countryService.getCountry(italyID.id).invoke()
      } yield {
        awaitSuccess() {
          for {
            response <- countryService.getCountries.invoke
          } yield {
            response.countries should contain allOf(belgium, france, italy)
          }
        }
      }).flatMap(identity) 
    }
  }

  def awaitSuccess[T](maxDuration: FiniteDuration = 10.seconds,
                      checkEvery: FiniteDuration = 100.milliseconds)(block: => Future[T]): Future[T] = {
    val checkUntil = System.currentTimeMillis() + maxDuration.toMillis

    def doCheck(): Future[T] = {
      block.recoverWith {
        case recheck if checkUntil > System.currentTimeMillis() =>
          val timeout = Promise[T]()
          server.application.actorSystem.scheduler.scheduleOnce(checkEvery) {
            timeout.completeWith(doCheck())
          }(server.executionContext)
          timeout.future
      }
    }

    doCheck()
  }
}
