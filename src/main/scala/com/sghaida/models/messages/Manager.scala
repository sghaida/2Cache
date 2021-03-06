package com.sghaida.models.messages

import akka.actor.ActorRef

object Manager {

  case class PartitionInfo(name: String, actorRef: ActorRef, size: Long)

  trait ManagerRelated

  case class Initialize(storeName: String, numberOfPartitions: Int)
  case class Status(storeName: String)
  case class Set[A,B](storeName: String, key: A, value: B, update: Boolean = false)
  case class Get[A](storeName: String, key: A)
  case class Remove[A](storeName: String, Key: A)

}
