package com.kps.equipes.country.api

import java.util.UUID
import play.api.libs.json.{Format, Json}

case class Country(id: UUID, name: String, isoCode: String)

object Country {
  implicit val format: Format[Country] = Json.format

  def apply(name: String, isoCode: String): Country = Country(UUID.randomUUID(), name, isoCode)
}
