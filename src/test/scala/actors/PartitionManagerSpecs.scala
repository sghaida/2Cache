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
import com.sghaida.models.messages.Partition
import com.sghaida.models.messages.Partition.{Done, StatusMessage}

import scala.collection.immutable

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

        case lst: Option[_] =>
          lst shouldBe a[Option[_]]
          lst.asInstanceOf[Option[_]] match {
          case Some(partitions) =>
            partitions shouldBe a[List[_]]
            partitions.asInstanceOf[List[_]].size shouldEqual 4
          case None => fail("couldn't create store")
        }
      }
    }


    "should not initialize store" in {
      probe.send(manager, Initialize("test-store", 4))

      probe.expectMsgPF() {
        case lst: Option[_] => lst shouldBe None
        case _ => fail(StoreAlreadyDefinedException("store should not be created"))
      }

    }

    "return partition status" in {
      probe.send(manager, Status("test-store"))
      probe.expectMsgPF(){
        case status: Option[_] =>
          status.isDefined shouldEqual true
          status.get.isInstanceOf[Map[_,_]] shouldBe true
          val res = status.get.asInstanceOf[Map[String, PartitionInfo]]
          res.keys.size shouldEqual 4
          res.count(keyValue => keyValue._2.size == 0) shouldEqual 4

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
        case result: Future[_] =>
          result onComplete{
            case Success(res) =>
              res shouldBe a[StatusMessage]
              res shouldEqual Partition.Done
            case Failure(ex) => fail(ex)
          }
        case ex:Exception => fail(ex)
      }
    }

    "retrieve saved data" in {
      probe.send(manager, Get("test-store", "test-key"))
      probe.expectMsgPF(){
        case result: Future[_] =>
          result onComplete{
            case Success(v) =>
              v shouldBe a[Option[_]]
              v.asInstanceOf[Option[_]] shouldBe defined
              v.asInstanceOf[Option[_]].get shouldEqual "test-value"
            case Failure(ex) => fail(ex)
          }
        case ex:Exception => fail(ex)
      }

    }

    "remove key/value from the store" in {
      probe.send(manager, Remove("test-store", "test-key"))
      probe.expectMsgPF(){
        case result: Future[_] =>
          result onComplete{
            case Success(res) =>
              res shouldBe a[StatusMessage]
              res shouldEqual Partition.Done
            case Failure(ex) => fail(ex)
          }
        case ex:Exception => fail(ex)
      }

      /* get the deleted key/value*/
      probe.send(manager, Get("test-store", "test-key"))
      probe.expectMsgPF(){
        case result: Future[_] =>
          result onComplete{
            case Success(v) =>
              v shouldBe a[Option[_]]
              v.asInstanceOf[Option[_]] shouldBe None
            case Failure(ex) => fail(ex)
          }
        case ex:Exception => fail(ex)
      }
    }

    "parallel insertion for many key/value" in {
      (1 to 1000).par.foreach{key =>
        probe.send(manager, Set("test-store", s"$key", s"${key+1}"))
        probe.expectMsgPF(){
          case result: Future[_] =>
            result onComplete{
              case Success(res) =>
                res shouldBe a[StatusMessage]
                res shouldEqual Partition.Done
              case Failure(ex) => fail(ex)
            }
          case ex:Exception => fail(ex)
        }

      }
    }

  }

}
