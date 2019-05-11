package com.sghaida.partitioners

import com.sghaida.mixins.Partitioner

import scala.reflect.ClassTag

class SimplePartitioner extends Partitioner {

  def HashPartitioner[A: ClassTag](key: A, numOfPartitions: Int): Int = {
    val digest = SimplePartitioner.md.digest(key.toString.getBytes)
    val partialHash = (BigInt(digest) >> 16).toLong

    math.abs(partialHash % numOfPartitions toInt)
  }
}

object SimplePartitioner {
  import java.security.MessageDigest
  val md: MessageDigest = MessageDigest.getInstance("MD5")
}
