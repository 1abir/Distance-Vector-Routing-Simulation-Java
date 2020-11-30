package cse.buet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


//Work needed
public class Client {
    public static void main(String[] args) throws InterruptedException {
        NetworkUtility networkUtility = new NetworkUtility("127.0.0.1", 4444);
        System.out.println("Connected to server");
        /**
         * Tasks
         */
        
        /*
        1. Receive EndDevice configuration from server
        2. Receive active client list from server
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the message and recipient IP address to server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
                            all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                    Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
        */
        Random random = new Random();
        Object device = networkUtility.read();
        EndDevice endDevice = null;
        List<IPAddress> activeClientIps = new ArrayList<>();
        if(device instanceof EndDevice){
            endDevice = (EndDevice) device;
            try {
                activeClientIps = (List<IPAddress>) networkUtility.read();
                if (activeClientIps.isEmpty()) {
                    networkUtility.write("fail");
                    System.out.println("Active client List is Empty");
                    return;
                }
            } catch (Exception e) {
                networkUtility.write("fail");
                System.out.println("Error Getting Active client List");
                e.printStackTrace();
                return;
            }
            networkUtility.write("success");
            for (int i = 0; i < 10; i++) {
                String message, specialMessage = "";
                message = "Message : " + i;
//                if (i == 20) {
                    specialMessage = "SHOW_ROUTE";
//                }

                int indx = random.nextInt(activeClientIps.size());
                if (activeClientIps.size() != 1)
                    while (activeClientIps.get(indx).getString().equals(endDevice.getIpAddress().getString())){
                        indx = random.nextInt(activeClientIps.size());
                    }
                Packet packet = new Packet(message, specialMessage, endDevice.getIpAddress(),activeClientIps.get(indx));
                networkUtility.write(packet);
                String acknowledgement = (String) networkUtility.read();
//                if(i==20){
                    System.out.println("Source : "+packet.getSourceIP()+" , Destination : "+packet.getDestinationIP());
                    System.out.println(acknowledgement);
//                }
            }
        }
        else {
            networkUtility.write("fail");
            System.out.println("Problem with Getting End Device Configuration");
        }


        networkUtility.closeConnection();
    }
}
