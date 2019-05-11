package com.sghaida.models.messages

object Partition {
  trait PartitionRelated
  trait statusMessage
  case object Dispose extends PartitionRelated
  case object Reset extends PartitionRelated
  case object Done extends PartitionRelated with statusMessage
  case object Failed extends PartitionRelated with statusMessage
  case class Set[A,B](key: A, value: B, update: Boolean = false) extends PartitionRelated
  case class Get[A](key: A) extends PartitionRelated
  case class Delete[A](key: A) extends PartitionRelated
}