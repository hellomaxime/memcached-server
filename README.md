# memcached-server

Memcached is an in-memory key-value store for small chunks of arbitrary data (strings, objects) from results of database calls, API calls, or page rendering.  

Coding challenge from : https://codingchallenges.fyi/challenges/challenge-memcached  

By default, the server listens on port `11211` but you can use the `-p <port>` option to change it.  

You can connect to the server with telnet : `telnet localhost 11211`  

This is a lite version that only implements core operations :
- set (with expiration time)
- get
- add
- replace
- append
- prepend

Server handles concurrent clients with threads.  

Lazy expiration is implemented for expiration time, the data is not removed when it expires but only when a client tries to access an expired key.  