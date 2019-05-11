package com.sghaida.exceptions

trait EngineException extends Exception {
  val msg: String
}

object EngineException {

  case class StoreNotFoundException(storeName: String) extends EngineException {
    override val msg: String = s"store: $storeName was not found"
  }

  case class StoreAlreadyDefinedException(storeName: String) extends EngineException {
    override val msg: String = s"store: $storeName is already defined"
  }

  case class PartitionNotFoundException(partitionName: String) extends EngineException {
    override val msg: String = s"partition: $partitionName was not found"
  }

  case class KeyNotFoundException(key: String) extends EngineException {
    override val msg: String = s"key: $key was not found"
  }
}
