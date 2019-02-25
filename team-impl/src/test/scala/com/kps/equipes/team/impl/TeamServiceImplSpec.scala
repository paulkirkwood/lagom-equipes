package com.kps.equipes.team.impl

import com.kps.equipes.team.api._
import com.kps.equipes.team.impl.service.TeamApplication
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest._
import java.util.UUID
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class TeamServiceImplSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val server = startServer(defaultSetup.withCassandra(true)) { ctx =>
    new TeamApplication(ctx) with LocalServiceLocator
  }

  val teamService = server.serviceClient.implement[TeamService]

  override protected def afterAll(): Unit = server.stop()

  "team-service" should {
    "create a team" in {
      for {
        answer   <- teamService.createTeam.invoke(CreateTeamRequest("Team Sky"))
        response <- teamService.getTeam(answer.id).invoke()
      } yield {
        response.name should equal("Team Sky")
        response.active should equal(true)
      }
    }

    "change a team's name" in {
      for {
        answer   <- teamService.createTeam.invoke(CreateTeamRequest("Sem-France Loire"))
        _        <- teamService.changeTeamName(answer.id).invoke(ChangeTeamNameRequest("Skil-Mavic"))
        response <- teamService.getTeam(answer.id).invoke()
      } yield {
        response.name shouldEqual("Skil-Mavic")
        response.active should equal(true)
      }
    }

    "disband a team" in {
      for {
        answer <- teamService.createTeam.invoke(CreateTeamRequest("KAS"))
        _      <- teamService.changeTeamStatus(answer.id).invoke(ChangeTeamStatusRequest(false))
        response <- teamService.getTeam(answer.id).invoke()
      } yield {
        response.active should equal(false)
      }
    }

    "find a team by it's name" in {
      for {
        answer   <- teamService.createTeam.invoke(CreateTeamRequest("Panasonic"))
        response <- teamService.getTeamByName("Panasonic").invoke()
      } yield {
        response.id should equal(answer.id)
      }
    }
  }

    //"fail with empty name on change team name" in {
    //  teamService.changeTeamName(answer.answer).invoke(api.TeamChangeNameFields("")).map { answer =>
    //    fail("Empty name should raise an error!")
    //  }.recoverWith {
    //    case (err: BadRequest) => err.exceptionMessage.detail should be("Team name cannot be empty!")
    //  }
    //}

    //"deactivate team" in {
    //  teamService.deactivateTeam(answer.answer).invoke().map { answer =>
    //    answer.answer should equal (answer.answer)
    //  }
    //}
    
    //"find team deactivated" in {
    //  teamService.getTeamIsActive(answer.answer).invoke.map { answer =>
    //    answer.active should equal (false)
    //  }
    //}

    //"activate team" in {
    //  teamService.activateTeam(answer.answer).invoke().map { answer =>
    //    answer.answer should equal (answer.answer)
    //  }
    //}

    //"find team activated" in {
    //  teamService.getTeamIsActive(answer.answer).invoke.map { answer =>
    //    answer.active should equal (true)
    //  } 
    //} 
  //}
}
