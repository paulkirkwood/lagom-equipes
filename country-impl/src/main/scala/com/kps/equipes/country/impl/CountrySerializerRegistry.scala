package com.kps.equipes.country.impl
       
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializerRegistry, JsonSerializer}

object CountrySerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(

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
