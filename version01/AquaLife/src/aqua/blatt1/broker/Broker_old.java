
package aqua.blatt1.broker;

import java.io.Serializable;
import java.net.InetSocketAddress;

import aqua.blatt1.common.Direction;
import aqua.blatt1.common.FishModel;
import aqua.blatt1.common.msgtypes.DeregisterRequest;
import aqua.blatt1.common.msgtypes.HandoffRequest;
import aqua.blatt1.common.msgtypes.RegisterRequest;
import aqua.blatt1.common.msgtypes.RegisterResponse;
import messaging.Endpoint;
import messaging.Message;

public class Broker {

    private Endpoint endPoint;
    private ClientCollection<InetSocketAddress> collection;
    private int tankId = 0;

    public Broker() {
        endPoint = new Endpoint(4711);
        collection = new ClientCollection<>();
    }

    public void broker() {
        while(true) {
            Message msg = endPoint.blockingReceive();
            Serializable payload = msg.getPayload();
            InetSocketAddress sender = msg.getSender();
            

            if (payload instanceof DeregisterRequest) {
                deregister(sender);
            } else if (payload instanceof RegisterRequest) {
                register(sender);
            } else if (payload instanceof HandoffRequest) {
                handoffFish(sender, (HandoffRequest) payload);

            }

        }

    }


    private void register(InetSocketAddress sender) {
        
        final int id = collection.indexOf(sender);

        if (id == -1 ) {
            String newId = "tank" + id; 
            collection.add(newId, sender);
            endPoint.send(sender, new RegisterResponse(newId));
        } else {
            System.out.println("Client already registered");
        }
    }


    private void deregister(InetSocketAddress sender) {

        final int id = collection.indexOf(sender);

        if (id != -1 ) {
            collection.remove(id);
            System.out.println("Client removed successfully.");
        }
        
    }

    private void handoffFish(InetSocketAddress sender, HandoffRequest payload) {

        final int id_oldClient =  collection.indexOf(sender);

        if (id_oldClient == -1 ) {
            System.out.println("goht nöööt weil nicht registered.");
        }

        FishModel fish = payload.getFish();
        if (fish.getDirection() == Direction.RIGHT) {
            endPoint.send(collection.getRightNeighorOf(id_oldClient), payload );
       } else {
            endPoint.send(collection.getLeftNeighorOf(id_oldClient), payload);
       }

    }


    public static void main(String args[]) {
        Broker brokeBoi = new Broker();
        brokeBoi.broker();
    }

}



