# Application to run benchmark between PostgreSQL vs MongoDb and Redis vs Hazelcast
## Some results from findAll() method
1. Hazelcast vs Redis + psql with 2000 entries (Windows machine):
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

2. Hazelcast vs Redis + psql with 2000 entries (macOS machine):
```
Db query took 54 ms and found 2000 elements
Redis save in cache took 1142 ms
Hazelcast save in cache took 455 ms
----------------------NEW REQUEST---------------------------
Hazelcast Cache query found 2000 elements
Hazelcast Cache query took 42 ms
Redis Cache query found 2000 elements
Redis Cache query took 388 ms
...
----------------------AFTER SOME REQUESTS---------------------------
Hazelcast Cache query found 2000 elements
Hazelcast Cache query took 20 ms
Redis Cache query found 2000 elements
Redis Cache query took 366 ms
```
Looks like Hazelcast is faster on writes and reads for lists of data.

3Test with 50_000 elements (macOS machine):
```
Db query took 135 ms and found 50000 elements
Redis save in cache took 22828 ms
Hazelcast save in cache took 8006 ms
----------------------NEW REQUEST---------------------------
Hazelcast Cache query found 50000 elements
Hazelcast Cache query took 224 ms
Redis Cache query found 50000 elements
Redis Cache query took 8055 ms
...
----------------------AFTER SOME REQUESTS---------------------------
Hazelcast Cache query found 50000 elements
Hazelcast Cache query took 147 ms
Redis Cache query found 2000 elements
Redis Cache query took 8193 ms
```

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

## PostgreSQL vs MongoDb findAll() request processing results from 2000 elements (Windows machine)
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

## Short load test results with k6 and caches active (Hazelcast and Redis but just using results from Redis)
1. Navigate to **test>java>com>wikicoding>pesqlredisbenchmark>k6load** and run the command
```bash
cat script.js | docker run --rm -i grafana/k6 run -
```

2. Results
```
checks.........................: 100.00% 112 out of 112
    data_received..................: 5.2 MB  187 kB/s
    data_sent......................: 9.3 kB  331 B/s
    http_req_blocked...............: avg=606.93µs min=7.7µs    med=13.1µs max=4.24ms  p(90)=3.26ms   p(95)=3.43ms
    http_req_connecting............: avg=555.81µs min=0s       med=0s     max=4ms     p(90)=3.1ms    p(95)=3.23ms
    http_req_duration..............: avg=2.88s    min=153.57ms med=2.66s  max=16.04s  p(90)=3.71s    p(95)=8.8s
      { expected_response:true }...: avg=2.88s    min=153.57ms med=2.66s  max=16.04s  p(90)=3.71s    p(95)=8.8s
    http_req_failed................: 0.00%   0 out of 112
    http_req_receiving.............: avg=5.69ms   min=253.4µs  med=5.15ms max=23.84ms p(90)=8.63ms   p(95)=10.99ms
    http_req_sending...............: avg=74.66µs  min=15.8µs   med=66µs   max=479.7µs p(90)=142.36µs p(95)=167.75µs
    http_req_tls_handshaking.......: avg=0s       min=0s       med=0s     max=0s      p(90)=0s       p(95)=0s
    http_req_waiting...............: avg=2.88s    min=153.2ms  med=2.66s  max=16.03s  p(90)=3.71s    p(95)=8.8s
    http_reqs......................: 112     3.983691/s
    iteration_duration.............: avg=3.89s    min=1.15s    med=3.67s  max=17.04s  p(90)=4.71s    p(95)=9.8s
    iterations.....................: 112     3.983691/s
    vus............................: 1       min=1          max=20
    vus_max........................: 20      min=20         max=20
```

## Conclusion
More study should be done, for example benchmark how fast writes are on Hazelcast and Redis, but as far as the current results shows PostgreSQL has faster reads than MongoDb and Hazelcast or Redis are well suited in terms of performance for getting single elements by its key. To store or get datasets it's better to not use cache or if using it, then Hazelcast shows better performance of retrieving data.