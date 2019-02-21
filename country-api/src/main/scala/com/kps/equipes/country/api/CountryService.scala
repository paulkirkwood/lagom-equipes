package com.kps.equipes.country.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import java.util.UUID
import play.api.libs.json.{Format, Json}
import scala.collection.immutable.Seq

trait CountryService extends Service {

  def createCountry: ServiceCall[CreateCountryRequest, CreateCountryResponse]
  def getCountry(id: UUID): ServiceCall[NotUsed, Country]
  //def getCountries(pageNo: Option[Int], pageSize: Option[Int]): ServiceCall[NotUsed,Seq[Country]]

  override final def descriptor = {
    import Service._
    named("country-service").withCalls(
      restCall(Method.POST,"/api/countries", createCountry),
      restCall(Method.GET,"/api/countries/:id", getCountry _)
      //restCall(Method.GET,"/api/countries?pageNo&pageSize", getCountries _),
    ).withAutoAcl(true)
  }
}

/**
  * Create
  */
case class CreateCountryRequest(name: String, isoCode: String)
object CreateCountryRequest {
  implicit val format: Format[CreateCountryRequest] = Json.format
}

case class CreateCountryResponse(id: UUID)
object CreateCountryResponse {
  implicit val format: Format[CreateCountryResponse] = Json.format
}

/**
  * Read
  */
case class Country(id: UUID, name: String, isoCode: String)
object Country {
  implicit val format: Format[Country] = Json.format
}
