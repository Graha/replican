replican
========

    Paxo based synchronous replicaiton management. Google Spanner based Key-Value Distributed Datastore. 


Currenty has very very simple implemation to show read and write with replication. 
Paxos been implemented with Apache Mina. While currently writes handled in-memory.

**Major Classes**,

* graha.replican.paxos.Client   -> Entry point to client for distrubuted storage
* graha.replican.paxos.Leader   -> accept from client and propose to pool of Acceptor 
* graha.replican.paxos.Acceptor -> each Acceptor accepts the proposed task in (Eg., Replication)
* graha.replican.paxos.Learner  -> exectutes the task in (Eg., Replication)



**TODO**,

Few major thinks needted to be done,

 * Recovery
 * Leader Election
 * Very High Velocity throughput testing
 * Heartbeating
 * Orchestration



to test possible operations,

    $ mvn clean package
    $ mvn exec:java -Dexec.mainClass="graha.replican.paxos.Acceptor" (in one terminal)
    $ mvn exec:java -Dexec.mainClass="graha.replican.paxos.Client" (on another terminal)


**Asychnronous change based replication**

    rsync type replication implementation been done
    

**Basic Design**

Watch Directory (Java 7 feature)
	if any change
	
		Added / Delete  
	
			Send Operation (Replicator to Replicants)
	
			Apply Operation (On Replicant)
	
			Ack (Status back to Replicator)
	
		Modify
	
			Unique Number (##REF##) for tagging change.
	
			Make Patch (rshync algo implementation) and tag it
	
			Async Write Local (.patch/filename.##REF##) && Send it out <-- For Replay
	
			Receive && Apply Patch (Remote) && Write Remote (same as Source)  <-- For Replay
	
			Ack (Remote)


Major classes,    

* graha.replican.async.Replicant  -> remote system where replication to be recieved and updated.
* graha.replican.async.Replicator -> source of where file get updated
* graha.replican.AysncReplication -> Service built above replicator


to test possible operations,

    $ mvn clean package
    $ mvn exec:java -Dexec.mainClass="graha.replican.async.Replicant" -Dexec.args="remote" //argument is folder name
    $ mvn exec:java -Dexec.mainClass="graha.replican.AsyncReplican" -Dexec.args="local"    //argument is folder name



Create a file,

    if (size > 0)
        request replicator for file content (local)
    else
        create an empty file
        
delete a file,

    delete the file from replican (remote)
    

update a file,

    1.request replicator for the checksum
    2.response will be checksum for pages of size 1M (for example file is 10MB then, array of 10 checksum ll be there)
    3.at remote checksum of local will be compared all delta chunk will be re-requested
    4.with replied chuncks updates will be patched 
    
    
