package com.sghaida.models.messages

import akka.actor.ActorRef

object Manager {

  case class PartitionInfo(name: String, actorRef: ActorRef, size: Long)

  trait ManagerRelated

  case class Initialize(storeName: String, numberOfPartitions: Int)
  case class Status(storeName: String)
  case class Balance(storeName: String)
  case class Add(storeName: String, numberOfPartitions: Int)
  case class Remove(storeName: String, partitionId: String)
  case class Set[A,B](storeName: String, key: A, value: B, update: Boolean = false)
  case class Get[A](storeName: String, key: A)

}
