### 2Cache

a small in memory distributed caching engine build on scala + akka 

the engine suppose to cover the following basic features then evolve from their

1. create a store based on name and number of partitions
2. delete a store and related partitions
3. respond to store status request with number of entries per partition
4. add new partition with auto-balance 
5. remove one partition with auto-balance
6. add key/value to the store
7. add multiple key/value
8. remove key/value from the store
9. remove multiple key/value
10. expire key/value after certain duration
11. update value for specific key
12. get value for specific key
13. get values for list of keys
14. all communication should go through the manager
15. all responses should be wrapped in future

as i go along i will mark whats done  __~~something done~~__
  
