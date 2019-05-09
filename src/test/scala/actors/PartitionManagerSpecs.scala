package actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.sghaida.actors.Models.{Manager, PartitionInfo}
import com.sghaida.actors.PartitionManager
import com.sghaida.exceptions.EngineException.StoreAlreadyDefinedException
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import org.scalatest.Matchers._

class PartitionManagerSpecs
  extends TestKit(ActorSystem("test-system"))
    with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = system.terminate()

  "Partition Manager" should {

    val manager = system.actorOf(PartitionManager.props[String, String])
    val probe = TestProbe()

    "initialize data store with partitions" in{

      probe.send(manager, Manager.Initialize("test-store", 4))

      probe.expectMsgPF() {

        case lst: Option[List[String]] => lst match {

          case Some(partitions) =>
            partitions.size shouldEqual 4
            partitions.count(_.contains("test-store")) shouldEqual 4

          case None =>
            assert(false, "couldn't create store")

        }
      }
    }


    "should not initialize store" in {
      probe.send(manager, Manager.Initialize("test-store", 4))

      probe.expectMsgPF() {
        case lst: Option[List[String]] => lst shouldBe None
        case _ => throw StoreAlreadyDefinedException("store should not be created")
      }

    }

    "return partition status" in {
      probe.send(manager, Manager.Status("test-store"))
      probe.expectMsgPF(){
        case status: Option[Map[String, PartitionInfo]] =>
          status.isDefined shouldEqual true
          status.get.keys.size shouldEqual 4
          status.get.count(keyValue => keyValue._2.size == 0) shouldEqual 4

      }
    }

    "should not return partition status" in {
      probe.send(manager, Manager.Status("test1-store"))
      probe.expectMsgPF(){
        case status: Option[_] =>
          status.isDefined shouldEqual false
      }
    }

  }

}
