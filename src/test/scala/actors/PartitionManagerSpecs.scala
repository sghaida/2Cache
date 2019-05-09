package actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class PartitionManagerSpecs
  extends TestKit(ActorSystem("test-system"))
    with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = system.terminate()

  "Partition Manager" should {

  }

}
