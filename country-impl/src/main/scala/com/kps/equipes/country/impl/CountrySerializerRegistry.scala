package com.kps.equipes.country.impl

import com.kps.equipes.country.api.Country
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
