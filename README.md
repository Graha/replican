replican
========

    rsync like replication implementation

**Operations**

Create a file,

    if (size > 0)
        tailed file content will be created
    else
        create an empty file
        
    reply the checkpoint for ledger
        
delete a file,

    delete the file from replican (remote) and clear up ledger
    

update a file, (currently only append, for log replication)

    delta content will be created from ledgered checkpoints
    sent to remote 
    replicated on remote 
    updated checkpoints will be ledgered
    
    
**commands**

    $ mvn clean package
    $ mvn exec:java -Dexec.mainClass="graha.replican.async.Replicant" -Dexec.args="remote" 
    $ mvn exec:java -Dexec.mainClass="graha.replican.watch.DirectoryWatchService" -Dexec.args="local"

