package actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import org.scalatest.Matchers._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

import com.sghaida.actors.PartitionManager
import com.sghaida.exceptions.EngineException.StoreAlreadyDefinedException
import com.sghaida.partitioners.SimplePartitioner
import com.sghaida.models.messages.Manager._

class PartitionManagerSpecs
  extends TestKit(ActorSystem("test-system"))
    with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = system.terminate()

  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  implicit val partitioner: SimplePartitioner = new SimplePartitioner()

  "Partition Manager" should {

    val manager = system.actorOf(PartitionManager.props[String, String])
    val probe = TestProbe()

    "initialize data store with partitions" in{

      probe.send(manager, Initialize("test-store", 4))

      probe.expectMsgPF() {

        case lst: Option[List[Int]] => lst match {

          case Some(partitions) => partitions.size shouldEqual 4
          case None => fail("couldn't create store")
        }
      }
    }


    "should not initialize store" in {
      probe.send(manager, Initialize("test-store", 4))

      probe.expectMsgPF() {
        case lst: Option[List[String]] => lst shouldBe None
        case _ => fail(StoreAlreadyDefinedException("store should not be created"))
      }

    }

    "return partition status" in {
      probe.send(manager, Status("test-store"))
      probe.expectMsgPF(){
        case status: Option[Map[String, PartitionInfo]] =>
          status.isDefined shouldEqual true
          status.get.keys.size shouldEqual 4
          status.get.count(keyValue => keyValue._2.size == 0) shouldEqual 4

      }
    }

    "should not return partition status" in {
      probe.send(manager, Status("test1-store"))
      probe.expectMsgPF(){
        case status: Option[_] =>
          status.isDefined shouldEqual false
      }
    }

    "save data to correct partition" in {
      probe.send(manager, Set("test-store", "test-key", "test-value"))
      probe.expectMsgPF(){
        case result: Future[statusMessage] =>
          result onComplete{
            case Success(_) =>
            case Failure(ex) => fail(ex)
          }
        case ex:Exception => fail(ex)
      }
    }

    "retrieve saved data" in {
      probe.send(manager, Get("test-store", "test-key"))
      probe.expectMsgPF(){
        case result: Future[Option[String]] =>
          result onComplete{
            case Success(v) =>
              v shouldBe defined
              v.get shouldEqual "test-value"
            case Failure(ex) => fail(ex)
          }
        case ex:Exception => fail(ex)
      }

    }

  }

}
