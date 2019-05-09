package com.sghaida.exceptions

trait EngineException extends Exception {
  val msg: String
}

object EngineException {
  case class StoreNotFoundException(msg: String) extends EngineException
  case class StoreAlreadyDefinedException(msg: String) extends EngineException
  case class PartitionNotFoundException(msg: String) extends EngineException
}
