package com.sghaida.mixins

import scala.reflect.ClassTag

trait Partitioner {
  def HashPartitioner[A: ClassTag](key: A, numOfPartitions: Int): Int
}

