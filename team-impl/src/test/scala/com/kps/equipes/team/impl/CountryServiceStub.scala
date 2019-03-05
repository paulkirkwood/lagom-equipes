package com.kps.equipes.team.impl

import com.kps.equipes.country.api.{Country, CountryService}
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class CountryServiceStub(implicit ec: ExecutionContext) extends CountryService {

  val unitedKingdom = Country(UUID.randomUUID(), "United Kingdom", "GBR")
  val france = Country(UUID.randomUUID(), "France", "FRA")
  val spain = Country(UUID.randomUUID(), "Spain", "ESP")
  val netherlands = Country(UUID.randomUUID(), "Netherlands", "NED")

  override def createCountry = ServerServiceCall { _ =>
    throw new NotImplementedError
  }

  override def getCountry(id: UUID) = ServerServiceCall { _ =>
    throw new NotImplementedError
  }

  override def getCountryByISOCode(isoCode: String) = ServerServiceCall { _ =>
    isoCode match {
      case "ESP" => Future(spain)
      case "FRA" => Future(france)
      case "GBR" => Future(unitedKingdom)
      case "NED" => Future(netherlands)
      case _     =>  Future.failed(NotFound(s"Country $isoCode not found"))
    }
  }

  override def getCountries = ServerServiceCall { _ =>
    throw new NotImplementedError
  }

  override def getCountryByName(name: String) = ServerServiceCall { _ =>
    name match {
      case "Spain"          => Future(spain)
      case "France"         => Future(france)
      case "Netherlands"    => Future(netherlands)
      case "United Kingdom" => Future(unitedKingdom)
      case _                => Future.failed(NotFound(s"Country $name not found"))
    }
  }
}
