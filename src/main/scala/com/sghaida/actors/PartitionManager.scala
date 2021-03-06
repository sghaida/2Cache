package com.sghaida.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props}
import akka.pattern.ask
import akka.actor.SupervisorStrategy.{Resume, Stop}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.reflect.ClassTag
import com.sghaida.models.messages.Manager.{Get, Initialize, PartitionInfo, Remove, Set, Status}
import com.sghaida.exceptions.EngineException.StoreNotFoundException
import com.sghaida.models.messages.Partition
import com.sghaida.partitioners.Partitioner

object PartitionManager{
  def props[A: ClassTag, B: ClassTag](implicit p: Partitioner): Props = Props(new PartitionManager[A,B]())
}

class PartitionManager[A: ClassTag, B: ClassTag](implicit partitioner: Partitioner) extends Actor with ActorLogging{

  implicit val timeout: Timeout = 1 second

  override def postStop(): Unit = log.debug("Stopped")
  override def preStart(): Unit = log.debug("Starting...")

  override def receive: Receive = partitionActivities(Map())

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 seconds) {
      case _: StoreNotFoundException => Resume
      case _: RuntimeException => Stop
    }

  def partitionActivities(info: Map[String, Map[Int, PartitionInfo]]): Receive = {

    case Initialize(name, numOfParts) => info.get(name) match {

      /* if store is defined then it can't be initialized and send back None else create the store
      * and send back the list of partitions names
      * */
      case Some(_) =>
        log.warning(s"[Initialize]  $name: is already defined")
        sender() ! None

      case _ => /* not store exist */
        val partitionsInfo = for (i <- 0 until numOfParts) yield {

          /* generate uuid -> take the first 4 digits -> append to the store name */
          val id = s"$name-${UUID.randomUUID().toString.take(4)}"

          /* create the actor ref for a specific partition*/
          val actorRef = context.actorOf(PartitionActor.props[A, B](id), s"$id")

          i -> PartitionInfo(id, actorRef, 0L)
        }

        /* convert to map */
        val partitionsMap = partitionsInfo.toMap
        val newStore = info + (name -> partitionsMap)

        sender() ! Some(partitionsMap.keys.toList)

        context.become(partitionActivities(newStore))
    }

    case Status(name) => info.get(name) match {
      case Some(status) =>
        sender() ! Some(status)
      case None =>
        log.warning(s"[Status] $name: is not defined")
        sender() ! None
    }

    case Set(name, key, value, update) =>
      val store = info.get(name)
      if (store.isEmpty) throw StoreNotFoundException(name)

      val partition = partitioner.HashPartitioner(key, store.get.keys.size)
      val partitionActor = store.get(partition).actorRef
      val saveResult = partitionActor ? (Partition Set(key = key, value = value, update = update))

      sender() ! saveResult

    case Get(name, key) =>
      val store = info.get(name)
      if (store.isEmpty) throw StoreNotFoundException(name)

      val partition = partitioner.HashPartitioner(key, store.get.keys.size)
      val partitionActor = store.get(partition).actorRef

      val getResult = partitionActor ? ( Partition Get(key=key))

      sender() ! getResult

    case Remove(name, key) =>
      val store = info.get(name)
      if (store.isEmpty) throw StoreNotFoundException(name)

      val partition = partitioner.HashPartitioner(key, store.get.keys.size)
      val partitionActor = store.get(partition).actorRef
      val deleteResult = partitionActor ? ( Partition Remove(key=key))
      sender() ! deleteResult

  }


}
