replican
========

    Paxo based synchronous replicaiton management. Google Spanner based Key-Value Distributed Datastore. 


to test possible operations,

$ mvn clean package

$ mvn exec:java -Dexec.mainClass="graha.replican.paxos.Acceptor" (in one terminal)

$ mvn exec:java -Dexec.mainClass="graha.replican.paxos.Client" (on another terminal)




