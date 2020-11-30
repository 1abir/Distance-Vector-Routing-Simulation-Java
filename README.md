
# Distance Vector Routing

# BANGLADESH UNIVERSITY OF ENGINEERING AND TECHNOLOGY

# Department of Computer Science and Engineering

# CSE 322 Offline Network Layer


## This Java Proect simulates simplified version of “Distance Vector Routing (DVR)”.  

1. A topology of routers is given as an input intopology.txt. File input/output is managed
    in the template. The topology contains the list of connected routers (routerId) and IP
    Addresses of all interfaces against each router. The first interface given in the file is always
    dedicated for connecting end devices.
2. OpenConstants.java. There are two defined constants. One isINFTYwhich models the
    infinite distance. The other one isLAMBDA, used to define the rate of altering the state of
    routers (A functioning router might be switched off or a router which was switched off might
    be turned on).LAMBDAmust be within[0,1]. The higher the value is, the more router state
    changes will occur. An instance ofRouterStateChanger.javadoes the job for you.
3. A server (NetworkLayerServer.java) first reads the topology and lists the interfaces of
    routers to connect end devices (client). Each interface is a key inclientInterfacesmap,
    where the value represents the number of end devices connected to that interface (initially
    0).
4. While reading the topology, the server creates instances ofRouter.


5. InNetworkLayerServer.java, an algorithm is implemented which can be considered
    similar toDVR. The pseudocode is given below:

```
while(convergence) {

/* convergence means no change in any routingTable before and after
executing the following for loop
for each router r
starting from the router with routerId = startingRouterId, in any
order */

    {
        1. T <- getRoutingTable of the router r
        2. N <- find routers which are the active neighbors of the current
            router r
        3. Update routingTable of each router t in N using the
        routing table of r [Hint: Use t.updateRoutingTable(r)]
    }

}
```

```
Caution: In real-time networking, the routers are isolated. So, the perfect modeling should
have been to implement each router as a separate thread. But here, we are controlling the
updates of routing table of all routers from the server from a single thread to make things
simple. Please note that this is not the exact version of DVR. It is a trade-off where you
will understand how DVR works and also things are a little bit easier for you to implement.
Here the algorithm starts with the first router (routerId = startingRouterId) sending its
routing table to its neighbors which are in “UP” state. The sequence of other routers does
not matter. Note that, It is made sure router state does not change until the
algorithm finishes executing.
```
6. You will find two functions inNetworkLayerServer.java: While implementingDVR(), they are implemented
   with split horizon and forced update as discussed in theory



7. Now, we will started connecting clients.

8. The client adjustingNetworkLayerServer.java and ServerThread.java is enabled.
    The pseudo-code below. Remember, We have major works in the
    tasks inServerThread.java.


Receive EndDevice configuration from server

[Adjustment in NetworkLayerServer.java: Server internally handles a list
of active clients.]


```
for(int i=0;i<100;i++) {
Generate a random message
    if(i==20) 
    {
        Send the message to server and a special request "SHOW_ROUTE"
        Router assigns a random receiver from active client list
        [Adjustment required in ServerThread.java].
        Display routing path, hop count and routing table of each router
        [You need to receive all the required info from the server in response
        to "SHOW_ROUTE" request]
    }
}

else {
    Client sends the message to Server, which [Adjustment required in
    ServerThread.java] assigns a random receiver from active client list.
}
```



If server can successfully send the message, client will get an
acknowledgement along with hop count. Otherwise, client will get a failure
message [dropped packet]


Average number of hops and drop rate i reported 

9. Use instances of Packet.java for sending and receiving messages or requests. You can insert,
    update, delete any attribute of Packet class if required.
10. Finally, write a report comparing
    average number of hops and drop rate for  
    LAMBDA = 0.01, 0.05, 0.10, 0.25, 0.50, 0.80. is give below.  
    Just show the stats in a table, nothing else.  

    ApplysimpleDVR()instead ofDVR()keepingLAMBDA = 0.10 and compare how drop
    rate changes because of implementing split horizon and forced update. Briefly put your
    reasoning.