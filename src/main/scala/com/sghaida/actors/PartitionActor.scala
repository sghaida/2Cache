package com.sghaida.actors

import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props}
import com.sghaida.models.messages.Partition.{Remove, Dispose, Done, Failed, Get, Reset, Set}
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
        log.debug(s"[SET - Key/Value] key: $a with the value $b has been updated")

      case Some(_) =>
        /* nothing to do */
        sender() ! Failed
        log.debug(s"[SET - Key/Value] key: $a with the value $b will not be updated because update is set to false")

      case None  =>
        /* add the new value*/
        sender() ! Done
        context.become(partitionActivities(id, store + (a -> b)))
        log.debug(s"[SET - Key/Value] key: $a with the value $b has been created")

    }

    case Get(key: A) =>
      sender ! store.get(key)
      log.debug(s"[GET] $key -> ${store.get(key)}")

    case Remove(key: A) =>
      if (store.isDefinedAt(key)) {
        sender() ! Done
        context.become(partitionActivities(id, store - key))
        log.debug(s"[REMOVE - Key] key: $key has been deleted")
      } else {
        sender() ! Failed
        log.error(s"[DELETE - KEY] key: $key was not found")
      }

    case Reset =>
      sender() ! Done
      context.become(partitionActivities(id, Map[A,B]()))
      log.debug(s"[RESET] has been done successfully")

    case Dispose =>
      sender() ! Done
      context.stop(self)

  }
}
