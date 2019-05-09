package com.sghaida.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props}
import Models.Manager.{Initialize, Status}
import Models.PartitionInfo
import akka.actor.SupervisorStrategy.Resume

import scala.concurrent.duration._
import scala.reflect.ClassTag

object PartitionManager{
  def props[A: ClassTag, B: ClassTag]: Props = Props(new PartitionManager[A,B]())
}

class PartitionManager[A: ClassTag, B: ClassTag] extends Actor with ActorLogging{

  override def postStop(): Unit = log.debug("Stopped")
  override def preStart(): Unit = log.debug("Starting...")

  override def receive: Receive = partitionActivities(Map())

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 seconds) {
      case _: Exception => Resume
    }

  def partitionActivities(info: Map[String, Map[String, PartitionInfo]]): Receive = {

    case Initialize(name, numOfParts) => info.get(name) match {

      /* if store is defined then it can't be initialized and send back None else create the store
      * and send back the list of partitions names
      * */
      case Some(_) =>
        log.warning(s"[Initialize]  $name: is already defined")
        sender() ! None

      case _ => /* not store exist */
        val partitionsInfo = for (i <- 1 to numOfParts) yield {

          /* generate uuid -> take the first 4 digits -> append to the store name */
          val id = s"$name-${UUID.randomUUID().toString.take(4)}"

          /* create the actor ref for a specific partition*/
          val actorRef = context.actorOf(PartitionActor.props[A, B](id), s"$id")

          id -> PartitionInfo(actorRef, 0L)
        }

        /* convert to map */
        val partitionsMap = partitionsInfo.toMap
        val newStore = info + (name -> partitionsMap)

        sender() ! Some(partitionsMap.keys.toList)

        context.become(partitionActivities(newStore))
    }


    case Status(storeName) => info.get(storeName) match {
      case Some(status) =>
        sender() ! Some(status)
      case None =>
        log.warning(s"[Status] $storeName: is not defined")
        sender() ! None
    }
  }
}
