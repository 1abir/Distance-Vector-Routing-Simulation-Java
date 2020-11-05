package cse.buet;//Work needed
import java.sql.Connection;
import java.util.*;

public class Router {
    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddresses;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private ArrayList<Integer> neighborRouterIDs;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state
    private Map<Integer, IPAddress> gatewayIDtoIP;
    private Map<Integer,RoutingTableEntry> routerIDtoRoutingTableEntry;
    public Router() {
        interfaceAddresses = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIDs = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p < 0.80) state = true;
        else state = false;

        numberOfInterfaces = 0;
    }

    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddresses, Map<Integer, IPAddress> gatewayIDtoIP) {
        this.routerId = routerId;
        this.interfaceAddresses = interfaceAddresses;
        this.neighborRouterIDs = neighborRouters;
        this.gatewayIDtoIP = gatewayIDtoIP;
        routingTable = new ArrayList<>();
        routerIDtoRoutingTableEntry = new HashMap<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p < 0.80) state = true;
        else state = false;

        numberOfInterfaces = interfaceAddresses.size();
    }

    @Override
    public String toString() {
        String string = "";
        string += "Router ID: " + routerId + "\n" + "Interfaces: \n";
        for (int i = 0; i < numberOfInterfaces; i++) {
            string += interfaceAddresses.get(i).getString() + "\t";
        }
        string += "\n" + "Neighbors: \n";
        for(int i = 0; i < neighborRouterIDs.size(); i++) {
            string += neighborRouterIDs.get(i) + "\t";
        }
        return string;
    }



    /**
     * Initialize the distance(hop count) for each router.
     * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable() {
        HashSet<Integer> neighbourSet = new HashSet<>(neighborRouterIDs);
        for(Router r: NetworkLayerServer.routers){
            int distance = Constants.INFINITY;
            int nextRouterId = -1;
            if(r.getRouterId() == this.routerId){
                distance = 0;
                nextRouterId = r.getRouterId();
            }
            else if(neighbourSet.contains(r.getRouterId())){
                nextRouterId = r.getRouterId();
                if(r.getState()){
                    distance = 1;
                }
            }
            RoutingTableEntry rEntry = new RoutingTableEntry(r.getRouterId(),distance,nextRouterId);
            routingTable.add(rEntry);
            routerIDtoRoutingTableEntry.put(r.getRouterId(),rEntry);
        }
    }

    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable() {
        routingTable.clear();
    }

    /**
     * Update the routing table for this router using the entries of Router neighbor
     * @param neighbor
     */
    public boolean updateRoutingTable(Router neighbor) {
        boolean returnValue = false;
        List<RoutingTableEntry> neighbourRoutingTable = neighbor.getRoutingTable();
        for(RoutingTableEntry rte: neighbourRoutingTable){
            int destinationRouterId = rte.getRouterId();
            RoutingTableEntry thisRTE = routerIDtoRoutingTableEntry.get(destinationRouterId);
            if(rte.getDistance()+1 < thisRTE.getDistance() && rte.getDistance() < Constants.INFINITY){
                returnValue = true;
                thisRTE.setDistance(rte.getDistance()+1);
                thisRTE.setGatewayRouterId(neighbor.getRouterId());
            }
        }
        return returnValue;
    }

    public boolean sfupdateRoutingTable(Router neighbor) {
        boolean returnValue = false;
        List<RoutingTableEntry> neighbourRoutingTable = neighbor.getRoutingTable();
        for(RoutingTableEntry rte: neighbourRoutingTable){
            int destinationRouterId = rte.getRouterId();
            RoutingTableEntry thisRTE = routerIDtoRoutingTableEntry.get(destinationRouterId);
            // force update
            if((neighbor.getRouterId()==thisRTE.getGatewayRouterId()) || (rte.getDistance()+1 < thisRTE.getDistance() && rte.getGatewayRouterId() !=  this.getRouterId())){

                if(thisRTE.getDistance()!=(rte.getDistance()+1) && rte.getDistance()< Constants.INFINITY){
                    returnValue = true;
//                if(rte.getDistance()< Constants.INFINITY) {
                    thisRTE.setDistance(rte.getDistance() + 1);
                    thisRTE.setGatewayRouterId(neighbor.getRouterId());
                }
            }
        }
        return returnValue;
    }

    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState() {
        state = !state;
        if(state) { initiateRoutingTable(); }
        else { clearRoutingTable(); }
    }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddresses() {
        return interfaceAddresses;
    }

    public void setInterfaceAddresses(ArrayList<IPAddress> interfaceAddresses) {
        this.interfaceAddresses = interfaceAddresses;
        numberOfInterfaces = interfaceAddresses.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIDs() {
        return neighborRouterIDs;
    }

    public Map<Integer, RoutingTableEntry> getRouterIDtoRoutingTableEntry() {
        return routerIDtoRoutingTableEntry;
    }

    public void setNeighborRouterIDs(ArrayList<Integer> neighborRouterIDs) { this.neighborRouterIDs = neighborRouterIDs; }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Map<Integer, IPAddress> getGatewayIDtoIP() { return gatewayIDtoIP; }

    public void printRoutingTable() {
        System.out.println("Router " + routerId);
        System.out.println("DestID Distance Nexthop");
        for (RoutingTableEntry routingTableEntry : routingTable) {
            System.out.println(routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId());
        }
        System.out.println("-----------------------");
    }
    public String strRoutingTable() {
        String string = "Router" + routerId + "\n";
        string += "DestID Distance Nexthop\n";
        for (RoutingTableEntry routingTableEntry : routingTable) {
            string += routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId() + "\n";
        }

        string += "-----------------------\n";
        return string;
    }

}
