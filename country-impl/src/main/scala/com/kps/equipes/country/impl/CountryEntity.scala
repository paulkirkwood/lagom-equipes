package com.kps.equipes.country.impl
  
import akka.Done
import com.kps.equipes.country.api.Country
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class CountryEntity extends PersistentEntity { 
 
  override type Command     = CountryCommand[_]
  override type Event       = CountryEvent
  override type State       = CountryState
  override def initialState = CountryState(None)

  override def behavior: Behavior = {
    case CountryState(none)          => newEntity
    case CountryState(Some(country)) => existingEntity
  }

  val onGetCountry = Actions().onReadOnlyCommand[GetCountry, GetCountryReply] {
    case (GetCountry(), ctx, state) => ctx.reply(GetCountryReply(state.country))
  }

  val newEntity = {
    Actions()
      .onCommand[CreateCountry, Done] {
        case (CreateCountry(country), ctx, state) =>
          ctx.thenPersist(CountryCreated(country))( _ => ctx.reply(Done))
      }
      .onEvent {
        case (CountryCreated(country), state) => CountryState(country)
    }
  }.orElse(onGetCountry)

  val existingEntity = {
    Actions()
      .onCommand[CreateCountry, Done] {
        case (CreateCountry(country), ctx, state) =>
        ctx.invalidCommand("Country already exists")
        ctx.done
      }
  }.orElse(onGetCountry)
}
