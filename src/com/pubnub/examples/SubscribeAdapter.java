package com.pubnub.examples;

import org.json.JSONObject;

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

    private void Produce (Object message, final Channel channelRabbitMQ){
		System.out.println(" [*] SubscribeAdapter : PRODUCE : inspecting message " + message);
    	try {
    		//for this demo, we will produce a message to RabbitMQ onbly if we received a valid number in the Amount element of the JSON message from PubNub
    		JSONObject jsobj = new JSONObject(message.toString());
    		if (jsobj.getDouble("Amount") >= 0 ){
    			System.out.println(" [*] SubscribeAdapter : PRODUCE : verified Amount: " + jsobj.getDouble("Amount"));
    			jsobj.append("Verifier", "SubscribeAdapter");
    			channelRabbitMQ.basicPublish( "", TASK_QUEUE_NAME,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        jsobj.toString().getBytes());
                System.out.println(" [+] SubscribeAdapter : PRODUCE : produced to " + 
                        TASK_QUEUE_NAME + " '" + jsobj.toString() + "'");
    		}
    		else{
    			System.out.println(" [-] SubscribeAdapter : PRODUCE : discarding due to invalid Amount in " + message);
    		}
    	} catch (Exception e) {
    		System.out.println(" [!] SubscribeAdapter : PRODUCE : discarding message due to exception: " + e);
    	}
    }
	
    private void subscribe(final String channelPubNub, final Channel channelRabbitMQ) {
    	Callback cbSubscribe = new Callback(){
            @Override
            public void connectCallback(String channelPubNub, Object message) {
            	System.out.println(" [*] SubscribeAdapter : SUBSCRIBE : CONNECT on channel:" + channelPubNub
                           + " : " + message.getClass() + " : "
                           + message.toString());
            }

            @Override
            public void disconnectCallback(String channelPubNub, Object message) {
            	System.out.println(" [*] SubscribeAdapter : SUBSCRIBE : DISCONNECT on channel:" + channelPubNub
                           + " : " + message.getClass() + " : "
                           + message.toString());
            }

            public void reconnectCallback(String channelPubNub, Object message) {
            	System.out.println(" [*] SubscribeAdapter : SUBSCRIBE : RECONNECT on channel:" + channelPubNub
                           + " : " + message.getClass() + " : "
                           + message.toString());
            }

            @Override
            public void successCallback(String channelPubNub, Object message) {
            	System.out.println(" [x] SubscribeAdapter : SUBSCRIBE : RECEIVED on channel:" + channelPubNub
                        + " : " + message.getClass() + " : "
                        + message.toString());
                Produce(message, channelRabbitMQ);
            }

            @Override
            public void errorCallback(String channelPubNub, PubnubError error) {
            	System.out.println(" [!] SubscribeAdapter : SUBSCRIBE : ERROR on channel " + channelPubNub
                           + " : " + error.toString());
            }
    	};
    	
        try {
        	pubnub.subscribe(channelPubNub, cbSubscribe);
        } catch (Exception e) {
        	System.out.println(" [!] SubscribeAdapter SUBSCRIBE : ERROR on channel " + channelPubNub
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