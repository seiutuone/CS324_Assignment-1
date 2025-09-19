import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class LCRMain {
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.out.println("Usage: java LCRMain <id> <port> <nextHost> <nextPort>");
            System.out.println("Example: java LCRMain 1 1099 localhost 1100");
            System.exit(1);
        }

        int id = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);
        String nextHost = args[2];
        int nextPort = Integer.parseInt(args[3]);

        try {
            // Create RMI registry on the specified port
            LocateRegistry.createRegistry(port);
            System.out.println("RMI registry created on port " + port);
        } catch (Exception e) {
            // Registry might already exist, continue
            System.out.println("Using existing registry on port " + port);
        }

        // Create the node instance
        NodeImpl node = new NodeImpl(id);

        // Bind the node to the registry (ring topology communication)
        Naming.bind("rmi://localhost:" + port + "/Node", node);
        System.out.println("Node " + id + " bound to registry and ready for ring communication");

        // Wait and attempt to connect to next node in ring
        Node next = null;
        int maxAttempts = 30; // 1 minute total wait time

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                next = (Node) Naming.lookup("rmi://" + nextHost + ":" + nextPort + "/Node");
                System.out.println("Successfully connected to next node in ring: " + nextHost + ":" + nextPort);
                break;
            } catch (Exception e) {
                System.out.println("Attempt " + attempt + "/" + maxAttempts +
                        ": Waiting for next node at " + nextHost + ":" + nextPort);
                Thread.sleep(2000); // Wait 2 seconds before retry
            }
        }

        if (next == null) {
            System.err.println("ERROR: Could not connect to next node after " + maxAttempts + " attempts.");
            System.err.println("Ensure ring topology is properly configured.");
            System.exit(1);
        }

        // Set up ring topology - each node only knows its successor
        node.setNext(next);
        System.out.println("Ring topology established for Node " + id);

        // Wait for all nodes to establish ring connections
        System.out.println("Waiting for all nodes to join the ring...");
        Thread.sleep(10000);

        // Initiate election (as per LCR protocol, any node can initiate)
        System.out.println("Node " + id + " starting LCR election process...");
        node.initiateElection();

        // Keep program running to observe the election process
        System.out.println("Node " + id + " is active in the ring. Election in progress...");
        System.out.println("Press Ctrl+C to exit.");

        // Keep the node alive to participate in election
        while (true) {
            Thread.sleep(5000);
            // Optionally print current leader status
            if (node.getLeader() != -1) {
                System.out.println("Current leader: Node " + node.getLeader());
            }
        }
    }
}