# Application to run benchmark between PostgreSQL vs MongoDb and Redis vs Hazelcast
## Some results from findAll() method
1. Hazelcast vs Redis + psql with 2000 entries:
```
Db query took 185 ms and found 2000 elements
Redis save in cache took 5221 ms
Hazelcast save in cache took 2174 ms
----------------------NEW REQUEST---------------------------
Hazelcast Cache query found 2000 elements
Hazelcast Cache query took 93 ms
Redis Cache query found 2000 elements
Redis Cache query took 1706 ms
...
----------------------AFTER SOME REQUESTS---------------------------
Hazelcast Cache query found 2000 elements
Hazelcast Cache query took 15 ms
Redis Cache query found 2000 elements
Redis Cache query took 1474 ms
```
Looks like Hazelcast is faster on writes and reads for lists of data.

## Some results from findById() method. Again it shows faster results on Hazelcast
1. First run
```
Redis Cache query took 578 ms
Person with name John Doe1 found in Redis Cache
Hazelcast Cache query took 39 ms
Hazelcast Cache query found John Doe1
```

2. Second run
```
Redis Cache query took 4 ms
Person with name John Doe1 found in Redis Cache
Hazelcast Cache query took 2 ms
Hazelcast Cache query found John Doe1
```

## PostgreSQL vs MongoDb findAll() request processing results from 2000 elements
1. PostgreSQL
```
1. 184 ms
2. 22 ms
3. 20 ms
4. 17 ms
5. 22 ms
6. 19 ms
7. 13 ms
8. 15 ms 
9. 13 ms
10. 12 ms
```
2. MongoDB
```
1. 482 ms
2. 56 ms
3. 51 ms
4. 50 ms 
5. 44 ms
6. 26 ms
7. 19 ms
8. 21 ms
9. 20 ms
10. 19 ms
```
In this case, it's possible to observe that **psql** in general shows faster reads comparing to **mongodb** with a dataset of 2000 elements.

3. Hazelcast
```
1. 6 ms
2. 2 ms
3. 2 ms
4. 2 ms
5. 2 ms
6. 2 ms
7. 2 ms
8. 2 ms
9. 2 ms
10. 2 ms
```
4. Redis
```
1. 3 ms
2. 2 ms
3. 2 ms 
4. 1 ms
5. 2 ms
6. 3 ms
7. 2 ms
8. 2 ms
9. 2 ms
10. 2 ms
```
Hazelcast and Redis are about the same when accessing just 1 key.

## Conclusion
More study should be done, for example benchmark how fast writes are on Hazelcast and Redis, but as far as the current results shows PostgreSQL has faster reads than MongoDb and Hazelcast or Redis are well suited in terms of performance for getting single elements by its key. To store or get datasets it's better to not use cache or if using it, then Hazelcast shows better performance of retrieving data.