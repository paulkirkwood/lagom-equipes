package com.kps.equipes.country.impl.eventsourcing
      
import com.kps.equipes.country.api.Country
import java.util.UUID
import play.api.libs.json._
              
case class CountryState(country: Option[Country])
               
object CountryState {
  implicit val format: Format[CountryState] = Json.format[CountryState]

  def apply(country: Country): CountryState = new CountryState(Some(country))
} 
