package com.kps.equipes.country.impl

import akka.Done
import com.kps.equipes.country.api.Country
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json._

/**
  * Commands
  */
sealed trait CountryCommand[R] extends ReplyType[R]

case class CreateCountry(country: Country) extends CountryCommand[Done]

object CreateCountry {
  implicit val format: Format[CreateCountry] = Json.format
}

case class GetCountry() extends CountryCommand[GetCountryReply]

object GetCountry {
  implicit val strictReads = Reads[GetCountry](json => json.validate[JsObject].filter(_.values.isEmpty).map(_ => GetCountry()))
  implicit val writes = OWrites[GetCountry](_ => Json.obj())
}

/**
  * Replies
  */
case class GetCountryReply(country: Option[Country])

object GetCountryReply {
  implicit val format: Format[GetCountryReply] = Json.format[GetCountryReply]
}
