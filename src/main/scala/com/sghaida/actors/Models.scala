package com.sghaida.actors

import akka.actor.ActorRef

object Models {

  case class PartitionInfo(actorRef: ActorRef, size: Long)

  object Partition{

    trait PartitionRelated

    case object Dispose extends PartitionRelated
    case object Reset extends PartitionRelated
    case object Done extends PartitionRelated
    case object Failed extends PartitionRelated

    case class InitializePartition(id: Int)
    case class Set[A,B](key: A, value: B, update: Boolean = false) extends PartitionRelated
    case class Get[A](key: A) extends PartitionRelated
    case class Delete[A](key: A) extends PartitionRelated
  }


  object Manager{

    trait ManagerRelated

    case object Done extends ManagerRelated

    case class Initialize(storeName: String, numberOfPartitions: Int)
    case class Status(storeName: String)

    case class Balance(storeName: String)
    case class Add(storeName: String)
    case class Remove(storeName: String, partitionId: String)

  }
}
