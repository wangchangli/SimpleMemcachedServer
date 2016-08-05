## What is this

This is a simple memcached server, support set/get/delete commands only.

## How to run

Step1.Download the code

    git clone git@github.com:wangchangli/SimpleMemcachedServer.git

Step2.Build
    
    mvn clean package

Step3.Run the server

    java -jar target/SimpleMemcachedServer-1.0-SNAPSHOT-jar-with-dependencies.jar 

Step4.Test the server with telnet or run the test with maven

    telnet 127.0.0.1 11211                                                                                                                                                                   1 ↵
    Trying 127.0.0.1...
    Connected to localhost.
    Escape character is '^]'.
    set key1 0 0 6
    value1
    STORED
    get key1
    VALUE key1 0 6 0
    value1
    END
    delete key1
    DELETED
    get key1
    END
    aa
    ERROR
    set 1 2
    CLIENT_ERROR Bad number of args passed
    set a b c d e
    CLIENT_ERROR Parse client value failed

## Benchmark
 
    java -cp target/SimpleMemcachedServer-1.0-SNAPSHOT-jar-with-dependencies.jar:target/SimpleMemcachedServer-1.0-SNAPSHOT-tests.jar com.cache.Benchmark "127.0.0.1:11211" 8 100000
    
