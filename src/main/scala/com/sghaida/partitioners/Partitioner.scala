package com.sghaida.partitioners

import scala.reflect.ClassTag

trait Partitioner {
  def HashPartitioner[A: ClassTag](key: A, numOfPartitions: Int): Int
}
