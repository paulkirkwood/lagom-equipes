package com.kps.equipes.country.impl
       
import akka.NotUsed
import com.kps.equipes.country.api
import com.kps.equipes.country.api.CountryService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, NotFound}
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.lightbend.lagom.scaladsl.persistence.{PersistentEntityRef, PersistentEntityRegistry}
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.collection.immutable.Seq

class CountryServiceImpl(persistentEntityRegistry: PersistentEntityRegistry,
                         session: CassandraSession
                        )(implicit ec: ExecutionContext) extends CountryService {
    
  private def refFor(id: UUID): PersistentEntityRef[CountryCommand[_]] =
    persistentEntityRegistry.refFor[CountryEntity](id.toString)
    
  override def createCountry() = ServiceCall { req =>
    val id = UUID.randomUUID()
    refFor(id).ask(CreateCountry(api.Country(id,req.name,req.isoCode))).map { _ => api.CreateCountryResponse(id) }
  }

  override def getCountry(id: UUID) = ServiceCall { req =>
    refFor(id).ask(GetCountry()).map(_.country.getOrElse(throw NotFound(s"Country $id not found")))
  }

  //private val DefaultPageSize = 10
  //override def getCountries(pageNo: Option[Int], pageSize: Option[Int]) = ServiceCall[NotUsed, Seq[api.Country]] { req =>
  //  val sql = "SELECT id, name, isoCode FROM countries LIMIT ?"
  //  session.select(sql,Integer.valueOf(pageSize.getOrElse(DefaultPageSize))).map { row =>
  //    api.Country(
  //      row.getUUID("id"),
  //      row.getString("name"),
  //      row.getString("isoCode")
  //    )
  //  }.runFold(Seq.empty[api.Country])((acc, e) => acc :+ e)
  //}
}
