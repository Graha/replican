replican
========

    Paxo based synchronous replicaiton management. Google Spanner based Key-Value Distributed Datastore. 


to test possible operations,

$ mvn clean package

$ mvn exec:java -Dexec.mainClass="graha.replican.paxos.Acceptor" (in one terminal)

$ mvn exec:java -Dexec.mainClass="graha.replican.paxos.Client" (on another terminal)



    rsync type replication implementation
    
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
    
    
