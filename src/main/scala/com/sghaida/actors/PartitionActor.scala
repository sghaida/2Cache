package com.sghaida.actors

import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props}
import com.sghaida.models.messages.Partition.{Delete, Dispose, Done, Failed, Get, Reset, Set}
import akka.actor.SupervisorStrategy.Escalate

import scala.concurrent.duration._
import scala.reflect.ClassTag

object PartitionActor{
  def props[A: ClassTag,B: ClassTag](id: String): Props = Props(new PartitionActor[A,B](id))
}

class PartitionActor[A: ClassTag, B: ClassTag](id: String) extends Actor with ActorLogging{

  override def postStop(): Unit = log.debug("Stopped")
  override def preStart(): Unit = log.debug("Starting....")

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 seconds) {
      case _: Exception => Escalate
    }

  override def receive: Receive = partitionActivities(id, Map())

  def partitionActivities(id: String, store: Map[A,B]): Receive = {

    case Set(a: A, b: B, update) => store.get(a) match {

      case Some(_) if update =>
        /* remove old and the new*/
        sender() ! Done
        context.become(partitionActivities(id, store - a + (a -> b)))

      case Some(_) =>
        /* nothing to do */
        sender() ! Failed

      case None  =>
        /* add the new value*/
        sender() ! Done
        context.become(partitionActivities(id, store + (a -> b)))

    }

    case Get(key: A) =>
      log.debug(s"$key -> ${store.get(key)}")
      sender ! store.get(key)

    case Delete(key: A) =>
      sender() ! Done
      context.become(partitionActivities(id, store - key ))


    case Reset =>
      sender() ! Done
      context.become(partitionActivities(id, Map[A,B]()))

    case Dispose =>
      sender() ! Done
      context.stop(self)

  }
}
