package cse.buet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerThread implements Runnable {

    NetworkUtility networkUtility;
    EndDevice endDevice;
    int dropped;
    int total_hop_count;
    int successful_packets;

    ServerThread(NetworkUtility networkUtility, EndDevice endDevice) {
        this.networkUtility = networkUtility;
        this.endDevice = endDevice;
        System.out.println("Server Ready for client " + NetworkLayerServer.clientCount);
        NetworkLayerServer.clientCount++;
        dropped =0;
        total_hop_count = 0;
        successful_packets = 0;
        new Thread(this).start();
    }

    @Override
    public void run() {
        /**
         * Synchronize actions with client.
         */
        
        /*
        Tasks:
        1. Upon receiving a packet and recipient, call deliverPacket(packet)
        2. If the packet contains "SHOW_ROUTE" request, then fetch the required information
                and send back to client
        3. Either send acknowledgement with number of hops or send failure message back to client
        */
        List<IPAddress> activeClientList = new ArrayList<>();
        for (EndDevice eD :
                NetworkLayerServer.endDevices) {
//            int routerId = NetworkLayerServer.deviceIDtoRouterID.get(eD.getDeviceID());
//            Router r = NetworkLayerServer.routerMap.get(routerId);
//            if(r.getState()){
                activeClientList.add(eD.getIpAddress());
//            }
        }
        networkUtility.write(endDevice);

        networkUtility.write(activeClientList);
        String response = (String) networkUtility.read();
        if(response.equals("fail")){
            System.out.println("Error sending Active clinet list");
            return;
        }
        List<Packet> packets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Packet packet = null;
            try {
                packet = (Packet) networkUtility.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
            packets.add(packet);
            deliverPacket(packet);
        }
        networkUtility.closeConnection();
//        System.out.println("ConnectionClosed");
        System.out.println("Lambda : "+Constants.LAMBDA+", AHC : "+total_hop_count*1.0/successful_packets+", drop rate : "+dropped+"\n");

    }


    public Boolean deliverPacket(Packet p) {
        /*
        1. Find the router s which has an interface
                such that the interface and source end device have same network address.
        2. Find the router d which has an interface
                such that the interface and destination end device have same network address.
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
                similarly, x forwards to the next gateway router y considering d as the destination,
                and eventually the packet reaches to destination router d.

            3(a) If, while forwarding, any gateway x, found from routingTable of router r is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t

            3(b) If, while forwarding, a router x receives the packet from router y,
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t

        4. If 3(a) occurs at any stage, packet will be dropped,
            otherwise successfully sent to the destination router
        */

        Router source = NetworkLayerServer.routerMap.get(NetworkLayerServer.deviceIDtoRouterID.get(endDevice.getDeviceID()));
        Router destination = ipToRouter(p.getDestinationIP());

        if(destination==null){
            networkUtility.write("Hop Count : "+p.getHopcount()+"\nDestination Router is down. Packet Dropped");
            dropped++;
            return false;
        }
        else {
            p.increamentHopCount();
            StringBuilder path = new StringBuilder();
            path.append("Path : ");
            path.append(source.getRouterId());
            RoutingTableEntry nextRTE = source.getRouterIDtoRoutingTableEntry().get(destination.getRouterId());
            Router nextRouter = NetworkLayerServer.routerMap.get(nextRTE.getGatewayRouterId());
            while (source.getRouterId() != destination.getRouterId()) {
                if (nextRTE.getDistance() == Constants.INFINITY || nextRTE.getGatewayRouterId()==-1 || !nextRouter.getState()) {
                    dropped++;
                    String msg;
                    if(nextRTE.getGatewayRouterId() == -1 || nextRTE.getDistance() == Constants.INFINITY) msg = "there is no path from Router "+ source.getRouterId() +" to router "+destination.getRouterId();
                    else {
                        msg = "Router with id : "+nextRouter.getRouterId()+" is down";
                        nextRTE.setDistance(Constants.INFINITY);
                        RouterStateChanger.islocked = true;
//                        System.out.println("DVR started");
                        NetworkLayerServer.DVR(source.getRouterId());
//                        NetworkLayerServer.simpleDVR(source.getRouterId());
//                        System.out.println("DVR finished");
                        RouterStateChanger.islocked = false;
                        synchronized (RouterStateChanger.msg) {
                            RouterStateChanger.msg.notifyAll();
                        }
                    }
                    networkUtility.write("Hop Count : "+p.getHopcount()+"\n"+path.toString() + "\n"+msg+". Packet Dropped");
                    return false;
                }

                if(nextRouter.getRouterIDtoRoutingTableEntry().get(source.getRouterId()).getDistance() == Constants.INFINITY){
                    nextRouter.getRouterIDtoRoutingTableEntry().get(source.getRouterId()).setDistance(1);
                    nextRouter.getRouterIDtoRoutingTableEntry().get(source.getRouterId()).setGatewayRouterId(source.getRouterId());
                    RouterStateChanger.islocked = true;
//                    System.out.println("DVR started");
                    NetworkLayerServer.DVR(nextRouter.getRouterId());
//                    NetworkLayerServer.simpleDVR(source.getRouterId());
//                    System.out.println("DVR finished");
                    RouterStateChanger.islocked = false;
                    synchronized (RouterStateChanger.msg) {
                        RouterStateChanger.msg.notifyAll();
                    }
                }

                p.increamentHopCount();

                if(p.getHopcount() > Constants.INFINITY){
                    dropped++;
                    networkUtility.write("Hop Count : "+p.getHopcount()+"\n"+path.toString() + "\n"+"TTL Expired"+". Packet Dropped");
                    return false;
                }

                path.append("->").append(nextRouter.getRouterId());
                source = nextRouter;
                nextRTE = source.getRouterIDtoRoutingTableEntry().get(destination.getRouterId());
                nextRouter = NetworkLayerServer.routerMap.get(nextRTE.getGatewayRouterId());
            }
            networkUtility.write("Hop Count : "+p.getHopcount()+"\n"+path.toString()+"\nPacket sent successfully");
            successful_packets+=1;
            total_hop_count+=p.getHopcount();
            return true;
        }
    }

    private Router ipToRouter(IPAddress ipAddress){
        String gateway = ipAddress.getBytes()[0] +"."+ ipAddress.getBytes()[1] +"."+ipAddress.getBytes()[2];
        for (Map.Entry<IPAddress,Integer> entry: NetworkLayerServer.interfacetoRouterID.entrySet()) {
            IPAddress gip = entry.getKey();
            int routerID = entry.getValue();
            String gatewayIP = gip.getBytes()[0] +"."+ gip.getBytes()[1] +"."+gip.getBytes()[2];
            if(gateway.equals(gatewayIP)){
                Router destination = NetworkLayerServer.routerMap.get(routerID);
                if (!destination.getState()) return null;
                return destination;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }
}
