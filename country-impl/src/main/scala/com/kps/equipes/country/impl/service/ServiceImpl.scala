package com.kps.equipes.country.impl.service
       
import akka.{Done,NotUsed}
import com.kps.equipes.country.api.{Country,
                                    CreateCountryRequest,
                                    CreateCountryResponse,
                                    GetCountriesResponse,
                                    CountryService}
import com.kps.equipes.country.impl.eventsourcing._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, NotFound}
import com.lightbend.lagom.scaladsl.persistence.{PersistentEntityRef, PersistentEntityRegistry}
import java.util.UUID
import scala.concurrent.ExecutionContext

class CountryServiceImpl(countryRepository: CountryRepository,
                         persistentEntityRegistry: PersistentEntityRegistry
                        )(implicit ec: ExecutionContext) extends CountryService {
    
  private def refFor(id: UUID): PersistentEntityRef[CountryCommand[_]] =
    persistentEntityRegistry.refFor[CountryEntity](id.toString)
    
  override def createCountry: ServiceCall[CreateCountryRequest, CreateCountryResponse] =
    ServiceCall { req =>
      val country = Country.apply(req.name, req.isoCode)
      refFor(country.id).ask(CreateCountry(country)).map { _ => CreateCountryResponse(country.id) }
    }

  override def getCountry(id: UUID): ServiceCall[NotUsed, Country] =
    ServiceCall { _ =>
      refFor(id).ask(GetCountry()).map(_.country.getOrElse(throw NotFound(s"Country $id not found")))
    }

  override def getCountries: ServiceCall[NotUsed, GetCountriesResponse] =
    ServiceCall { _ =>
      countryRepository.getCountries.map(countries => GetCountriesResponse(countries))
    }

  override def getCountryByName(name: String): ServiceCall[NotUsed, Country] =
    ServiceCall { _ =>
      countryRepository.getCountryByName(name).map(country =>
        Country(country.get.id, country.get.name, country.get.isoCode))
    }

  override def getCountryByISOCode(isoCode: String): ServiceCall[NotUsed, Country] =
    ServiceCall { _ =>
      countryRepository.getCountryByISOCode(isoCode).map(country =>
        Country(country.get.id, country.get.name, country.get.isoCode))
    }
}
