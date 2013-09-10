package com.pubnub.examples;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import com.pubnub.api.*;

import org.json.JSONException;
import org.json.JSONObject;

public class PublishAdapter {
    //messages produced by producerClient for PublishAdapter to publish to PubNub
    private static final String TASK_QUEUE_NAME = "task_queue_outbound_durable";
    private static final String publish_key = "demo";
    private static final String subscribe_key = "demo";
    //In this demo, we will only publish to PubNub if "Amount" field exceeds the Threshold variable
    private static final double threshold = 1000000.00;
    //decide whether or not to publish to PubNub
    private static boolean needToPublish (double amount){
        if (amount > threshold )
            return true;
        else
            return false;
    }

    public static void main(String[] argv) throws Exception {
        String channelName = "rabbitWorker";
        Pubnub pubnub = new Pubnub(publish_key, subscribe_key);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //configure message queues as durable
        boolean durable = true;
        channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
        System.out.println(" [*] PublishAdapter : waiting for messages. To exit press CTRL+C");
        //dispatch messages fairly rather than round-robin by waiting for ack before sending next message
        //be careful because queue can fill up if all workers are busy
        int prefetchCount = 1;
        channel.basicQos(prefetchCount);
        //ensure that an explicit ack is sent from worker before removing from the queue
        boolean autoAck = false;
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(TASK_QUEUE_NAME, autoAck, consumer);
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [x] PublishAdapter : received '" + message + "'");
            try {
                JSONObject jsobj = new JSONObject(message);
                //execute some business logic to decide whether or not to publish to PubNub
                if (needToPublish(jsobj.getDouble("Amount")))
                {
                    System.out.println(" [*] PublishAdapter : forwarding, Amount (" + jsobj.getDouble("Amount") + ") exceeds threshold (" + threshold + ") so let's publish to PubNub");
                    jsobj.put("Threshold", threshold);
                    jsobj.append("Verifier", "PublishAdapter");
                    pubnub.publish(channelName, jsobj, new Callback(){
                        public void successCallback(String channel, Object message) {
                            System.out.println(" [+] PublishAdapter : published to PubNub " + message);
                        }
                        public void errorCallback(String channel, PubnubError error) {
                            System.out.println(" [!] PublishAdapter : error " + error);
                        }
                    });
                }
                else
                    System.out.println(" [-] PublishAdapter : discarding, Amount (" + jsobj.getDouble("Amount") + ") is below threshold (" + threshold + ") so let's not publish to PubNub");
            }catch (JSONException e) {
                    System.out.println(" [!] PublishAdapter : error " + e);
            }
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
}