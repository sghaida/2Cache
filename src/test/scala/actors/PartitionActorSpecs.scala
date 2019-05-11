package actors

import scala.util.{Failure, Success}
import scala.concurrent.duration._

import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout

import com.sghaida.models.messages.Partition
import com.sghaida.actors.PartitionActor

class PartitionActorSpecs extends TestKit(ActorSystem("test-system"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = system.terminate()

  val parent = TestProbe()
  val partition: ActorRef = system.actorOf(PartitionActor.props[String, String]("test-a2c1"))

  "Partition Actor" should {

    "add key/value to partition" in {
      parent.send(partition, Partition.Set("1", "value 1"))
      parent.expectMsg(Partition.Done)
    }

    "get value for key in partition" in {
      parent.send(partition, Partition.Get("1"))
      parent.expectMsg(Some("value 1"))
    }

    "remove from partition" in {
      parent.send(partition, Partition.Delete("1"))
      parent.expectMsg(Partition.Done)

    }

    "update value in partition" in {
      parent.send(partition, Partition.Set("1", "value 1"))
      parent.send(partition, Partition.Set("1", "value 2", update = true))
      parent.send(partition, Partition.Get("1"))

      parent.expectMsg(Partition.Done)
      parent.expectMsg(Partition.Done)
      parent.expectMsg(Some("value 2"))

    }

    "value is not updated for key in partition" in {
      parent.send(partition, Partition.Set("1", "value 1", update = true))
      parent.send(partition, Partition.Set("1", "value 2"))
      parent.send(partition, Partition.Get("1"))

      parent.expectMsg(Partition.Done)
      parent.expectMsg(Partition.Failed)
      parent.expectMsg(Some("value 1"))
    }

    "empty the partition" in {
      parent.send(partition, Partition.Reset)
      parent.send(partition, Partition.Get("1"))

      parent.expectMsg(Partition.Done)
      parent.expectMsg(None)
    }

    "dispose the partition" in {

      implicit val timeout: Timeout = 1 second
      import system.dispatcher
      parent.send(partition, Partition.Dispose)

      parent.expectMsg(Partition.Done)
      system.actorSelection("test-a2c1").resolveOne onComplete {
        case Success(_) =>
          assert( false, "the partition hasn't been terminated")
        case Failure(ex) => assert(ex.getMessage.contains("Actor not found for"))
      }
    }
  }
}
