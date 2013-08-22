package com.pubnub.examples;

//import org.json.JSONArray;
//import org.json.JSONObject;
//import java.util.Hashtable;
//import java.util.Scanner;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.pubnub.api.*;

public class ProducerServer {

	//messages subscribed from PubNub by producerServer for consumerClient to consumePubNub
	private static final String TASK_QUEUE_NAME = "task_queue_inbound_durable";
    static Pubnub pubnub;
    private static final String publish_key = "demo";
    private static final String subscribe_key = "demo";
    String channelPubNub = "rabbitWorker";
    
    public ProducerServer() {
    	//TODO initialize constructor
    }

	private void notifyUser(Object message) {
	    System.out.println(message.toString());
	}

	private void notifyUser(Object message, final Channel channelRabbitMQ) {
	    System.out.println(message.toString());
        try{
            channelRabbitMQ.basicPublish( "", TASK_QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.toString().getBytes());
            System.out.println(" [x] producerServer produced to " + 
                    TASK_QUEUE_NAME + " '" + message.toString() + "'");
            doWork(message.toString());
        }
        catch (Exception e){
        	System.out.println(" [!] producerServer error" + e);
        }
	    
	}
	
    private void subscribe(final String channelPubNub, final Channel channelRabbitMQ) {
    	Callback cbSubscribe = new Callback(){
            @Override
            public void connectCallback(String channelPubNub, Object message) {
                notifyUser(" [*] producerServer SUBSCRIBE : CONNECT on channel:" + channelPubNub
                           + " : " + message.getClass() + " : "
                           + message.toString());
            }

            @Override
            public void disconnectCallback(String channelPubNub, Object message) {
                notifyUser(" [*] producerServer SUBSCRIBE : DISCONNECT on channel:" + channelPubNub
                           + " : " + message.getClass() + " : "
                           + message.toString());
            }

            public void reconnectCallback(String channelPubNub, Object message) {
                notifyUser(" [*] producerServer SUBSCRIBE : RECONNECT on channel:" + channelPubNub
                           + " : " + message.getClass() + " : "
                           + message.toString());
            }

            @Override
            public void successCallback(String channelPubNub, Object message) {
                notifyUser(" [*] producerServer SUBSCRIBE : " + channelPubNub + " : "
                           + message.getClass() + " : " + message.toString(), channelRabbitMQ);
            }

            @Override
            public void errorCallback(String channelPubNub, PubnubError error) {

                /*

                # Switch on error code, see PubnubError.java

                if (error.errorCode == 112) {
                    # Bad Auth Key!
                    unsubscribe, get a new auth key, subscribe, etc...
                } else if (error.errorCode == 113) {
                    # Need to set Auth Key !
                    unsubscribe, set auth, resubscribe
                }

                */

                notifyUser(" [!] producerServer SUBSCRIBE : ERROR on channel " + channelPubNub
                           + " : " + error.toString());
            }
    	};
    	
        try {
        	pubnub.subscribe(channelPubNub, cbSubscribe);
        } catch (Exception e) {
        }
    }
    
    public void startProducerServer() throws Exception {
    	pubnub = new Pubnub(publish_key, subscribe_key);
    	
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        final Channel channelRabbitMQ = connection.createChannel();

        //configure message queues as durable
        boolean durable = true;

        channelRabbitMQ.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);

        subscribe(channelPubNub, channelRabbitMQ);
        
        //let's keep the RabbitMQ channel open indefinitely
        //channelRabbitMQ.close();
        //connection.close();
    }
    
	private static void doWork(String task) throws InterruptedException {
        for (char ch: task.toCharArray()) {
          if (ch == '.') Thread.sleep(1000);
        }
      }


    public static void main(String[] argv) throws Exception {
    	ProducerServer ps = new ProducerServer();
    	ps.startProducerServer();
    }
}