package com.kps.equipes.country.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}
import java.util.UUID

trait CountryService extends Service {

  def createCountry: ServiceCall[CreateCountryRequest, CreateCountryResponse]
  def getCountry(id: UUID): ServiceCall[NotUsed, Country]
  def getCountryByName(name: String): ServiceCall[NotUsed, Country]
  def getCountryByISOCode(isoCode: String): ServiceCall[NotUsed, Country]
  def getCountries: ServiceCall[NotUsed, GetCountriesResponse]

  override final def descriptor = {
    import Service._
    named("country-service").withCalls(
      restCall(Method.POST,"/api/countries", createCountry),
      restCall(Method.GET,"/api/countries/:id", getCountry _),
      restCall(Method.GET,"/api/countries/name/:name", getCountryByName _),
      restCall(Method.GET,"/api/countries/isocode/:isoCode", getCountryByISOCode _),
      restCall(Method.GET,"/api/countries", getCountries _)
    ).withAutoAcl(true)
  }
}

/**
  * Requests
  */
case class CreateCountryRequest(name: String, isoCode: String)
object CreateCountryRequest {
  implicit val format: Format[CreateCountryRequest] = Json.format
}

/**
  * Responses
  */
case class CreateCountryResponse(id: UUID)
object CreateCountryResponse {
  implicit val format: Format[CreateCountryResponse] = Json.format
}

case class GetCountriesResponse(countries: Seq[Country])
object GetCountriesResponse {
  implicit val format: Format[GetCountriesResponse] = Json.format
}
