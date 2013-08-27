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

public class SubscribeAdapter {

	//messages subscribed from PubNub by SubscribeAdapter for consumerClient to consumePubNub
	private static final String TASK_QUEUE_NAME = "task_queue_inbound_durable";
    static Pubnub pubnub;
    private static final String publish_key = "demo";
    private static final String subscribe_key = "demo";
    String channelPubNub = "rabbitWorker";
    
    public SubscribeAdapter() {
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
            System.out.println(" [x] SubscribeAdapter produced to " + 
                    TASK_QUEUE_NAME + " '" + message.toString() + "'");
        }
        catch (Exception e){
        	System.out.println(" [!] SubscribeAdapter error" + e);
        }
	    
	}
	
    private void subscribe(final String channelPubNub, final Channel channelRabbitMQ) {
    	Callback cbSubscribe = new Callback(){
            @Override
            public void connectCallback(String channelPubNub, Object message) {
                notifyUser(" [*] SubscribeAdapter SUBSCRIBE : CONNECT on channel:" + channelPubNub
                           + " : " + message.getClass() + " : "
                           + message.toString());
            }

            @Override
            public void disconnectCallback(String channelPubNub, Object message) {
                notifyUser(" [*] SubscribeAdapter SUBSCRIBE : DISCONNECT on channel:" + channelPubNub
                           + " : " + message.getClass() + " : "
                           + message.toString());
            }

            public void reconnectCallback(String channelPubNub, Object message) {
                notifyUser(" [*] SubscribeAdapter SUBSCRIBE : RECONNECT on channel:" + channelPubNub
                           + " : " + message.getClass() + " : "
                           + message.toString());
            }

            @Override
            public void successCallback(String channelPubNub, Object message) {
                notifyUser(" [*] SubscribeAdapter SUBSCRIBE : " + channelPubNub + " : "
                           + message.getClass() + " : " + message.toString(), channelRabbitMQ);
            }

            @Override
            public void errorCallback(String channelPubNub, PubnubError error) {
                notifyUser(" [!] SubscribeAdapter SUBSCRIBE : ERROR on channel " + channelPubNub
                           + " : " + error.toString());
            }
    	};
    	
        try {
        	pubnub.subscribe(channelPubNub, cbSubscribe);
        } catch (Exception e) {
        	notifyUser(" [!] SubscribeAdapter SUBSCRIBE : ERROR on channel " + channelPubNub
                    + " : " + e.toString());
        }
    }
    
    public void startSubscribeAdapter() throws Exception {
    	pubnub = new Pubnub(publish_key, subscribe_key);	
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        final Channel channelRabbitMQ = connection.createChannel();
        //configure message queues as durable
        boolean durable = true;
        channelRabbitMQ.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
        subscribe(channelPubNub, channelRabbitMQ);
    }


    public static void main(String[] argv) throws Exception {
    	SubscribeAdapter ps = new SubscribeAdapter();
    	ps.startSubscribeAdapter();
    }
}