### 2Cache

a small in memory distributed caching engine build on scala + akka 

the engine suppose to cover the following basic features then evolve from their

1. ~~create a store based on name and number of partitions~~
2. delete a store and related partitions
3. ~~respond to store status request with number of entries per partition~~ 
4. ~~add key/value to the store~~
5. add multiple key/value
6. remove key/value from the store
7. remove multiple key/value
8. expire key/value after certain duration
9. ~~update value for specific key~~
10. ~~get value for specific key~~
11. get values for list of keys

as i go along i will mark whats done  __~~something done~~__

#### basic usage

```scala

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

import com.sghaida.models.messages.Manager._
import com.sghaida.actors.{PartitionActor, PartitionManager}
import com.sghaida.partitioners.SimplePartitioner

// initialize actor system
val system = ActorSystem("caching-engine")
implicit val executionContext: ExecutionContextExecutor = system.dispatcher
implicit val timeout: Timeout = 5 seconds

implicit val partitioner: SimplePartitioner = new SimplePartitioner()

// initialize datastore with name and number of partitions 
val storeManager = system.actorOf(PartitionManager.props[String, String])
val partitions = (storeManager ? Initialize("test", 4)).mapTo[Option[List[String]]]

// check if the store has been created successfully 
partitions onComplete{
    case Success(values) => values.foreach(println(_))
    case Failure(ex) => println(ex.getMessage)
  }

```

the following messages is available for the manager so far.
```scala
case class Initialize(storeName: String, numberOfPartitions: Int)
case class Status(storeName: String)
case class Set[A,B](storeName: String, key: A, value: B, update: Boolean = false)
case class Get[A](storeName: String, key: A)
``` 
  
