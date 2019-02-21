package com.kps.equipes.country.impl
  
import com.kps.equipes.country.api.Country
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag}
import play.api.libs.json.{Format, Json}
      
object CountryEvent {
  val Tags = AggregateEventTag.sharded[CountryEvent](3)
}     
    
sealed trait CountryEvent extends AggregateEvent[CountryEvent] {
  override def aggregateTag: AggregateEventShards[CountryEvent] = CountryEvent.Tags
} 
    
case class CountryCreated(country: Country) extends CountryEvent
    
object CountryCreated {
  implicit val format: Format[CountryCreated] = Json.format[CountryCreated]
}   
