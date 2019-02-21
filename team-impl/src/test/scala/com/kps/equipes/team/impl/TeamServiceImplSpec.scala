package com.kps.equipes.team.impl

import com.kps.equipes.team.api._
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest._
import java.util.UUID
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class TeamServiceImplSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val server = startServer(defaultSetup.withCassandra(true)) { ctx =>
    new TeamApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[TeamService]

  override protected def afterAll(): Unit = server.stop()

  "team-service" should {
    "create a team" in {
      for {
        teamID <- client.createTeam.invoke(CreateTeamRequest("Team Sky"))
        response <- client.getTeam(teamID.id).invoke()
      } yield {
        response.name should equal("Team Sky")
        response.active should equal(true)
      }
    }

    "handle duplicate create requests" in {
      client.createTeam.invoke(CreateTeamRequest("Panasonic"))
      client.createTeam.invoke(CreateTeamRequest("Panasonic")).map { answer =>
        fail("Duplicate create request should raise an error")
      }.recoverWith {
        case (err: BadRequest) => err.exceptionMessage.detail should be("Team 'Panasonic' already exists")
      }
    }
  } 

    //"change a team's name" in {
    //  client.changeTeamName(teamID.teamID).invoke(api.TeamChangeNameFields("Skil")).map { answer =>
    //    answer.teamID should equal (teamID.teamID)
    //  }
    //}

    //"fail with empty name on change team name" in {
    //  client.changeTeamName(teamID.teamID).invoke(api.TeamChangeNameFields("")).map { answer =>
    //    fail("Empty name should raise an error!")
    //  }.recoverWith {
    //    case (err: BadRequest) => err.exceptionMessage.detail should be("Team name cannot be empty!")
    //  }
    //}

    //"deactivate team" in {
    //  client.deactivateTeam(teamID.teamID).invoke().map { answer =>
    //    answer.teamID should equal (teamID.teamID)
    //  }
    //}
    
    //"find team deactivated" in {
    //  client.getTeamIsActive(teamID.teamID).invoke.map { answer =>
    //    answer.active should equal (false)
    //  }
    //}

    //"activate team" in {
    //  client.activateTeam(teamID.teamID).invoke().map { answer =>
    //    answer.teamID should equal (teamID.teamID)
    //  }
    //}

    //"find team activated" in {
    //  client.getTeamIsActive(teamID.teamID).invoke.map { answer =>
    //    answer.active should equal (true)
    //  } 
    //} 
  //}
}
