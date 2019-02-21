package com.kps.equipes.team.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.kps.equipes.team.api.Team
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.InvalidCommandException
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{PersistentEntityTestDriver, ServiceTest}
import java.util.UUID
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class TeamEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  val system = ActorSystem("TeamEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(TeamSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
 
  def withTestDriver(block: PersistentEntityTestDriver[TeamCommand[_], TeamEvent, TeamState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new TeamEntity, "test-team-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "The team entity" should {

    "not be initialized by default" in withTestDriver { driver =>
      val outcome = driver.run(GetTeam())
      outcome.replies should contain only GetTeamReply(None)
    }

    "create a team" in withTestDriver { driver =>
      val outcome = driver.run(CreateTeam(Team.apply("Team Sky")))
      outcome.events.size shouldBe 1
      outcome.events.head shouldBe an[TeamCreated]
      outcome.replies should contain only Done
    }

    "get team" in withTestDriver {driver =>
      val carrera = Team.apply("Carrera")
      driver.run(CreateTeam(carrera))
      val outcome = driver.run(GetTeam())
      outcome.replies should contain only GetTeamReply(Some(carrera))
      outcome.events.size should ===(0)
    }

    "not update team name if not initialized" in withTestDriver { driver =>
      val outcome = driver.run(ChangeTeamName("Panasonic"))
      outcome.replies should contain only InvalidCommandException("Team test-team-1 is not yet created")
    }

    "updating a team name" in withTestDriver { driver =>
      val sem = Team.apply("Sem-France Loire")
      driver.run(CreateTeam(sem))
      val outcome = driver.run(ChangeTeamName("Skil-Mavic"))
      outcome.replies should contain only Done
      outcome.events(0) should matchPattern { case TeamNameChanged(Team(sem.id, "Skil-Mavic", sem.active)) => }
    }

    "ignore duplicate name changes" in withTestDriver { driver =>
      driver.run(CreateTeam(Team.apply("Sem-France Loire")))
      driver.run(ChangeTeamName("Skil-Mavic"))
      val outcome = driver.run(ChangeTeamName("Skil-Mavic"))
      outcome.replies should contain only Done
      outcome.events.size should ===(0)
    }

    "allow changing a team's status" in withTestDriver { driver =>
      val kas = Team.apply("Kas")
      driver.run(CreateTeam(kas))
      val outcome = driver.run(ChangeTeamStatus(false))
      outcome.replies should contain only Done
      outcome.events(0) should matchPattern { case TeamStatusChanged(Team(kas.id, kas.name, false)) => }
    }
  }
}
