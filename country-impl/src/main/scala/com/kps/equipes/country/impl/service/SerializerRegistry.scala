package com.kps.equipes.country.impl.service

import com.kps.equipes.country.api.Country
import com.kps.equipes.country.impl.eventsourcing._
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializerRegistry, JsonSerializer}

object CountrySerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(

    /**
      * Entity
      */
    JsonSerializer[Country],

    /**
      * State
      */
    JsonSerializer[CountryState],

    /**
      * Commands
      */
    JsonSerializer[CreateCountry], 
    JsonSerializer[GetCountry], 
  
    /**
      * Replies
      */
    JsonSerializer[GetCountryReply], 

    /**
      * Events
      */ 
    JsonSerializer[CountryCreated]
  )
} 
