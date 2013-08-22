package com.pubnub.examples;

//import org.json.JSONArray;
//import org.json.JSONObject;
//import java.util.Hashtable;
//import java.util.Scanner;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import com.pubnub.api.*;

public class ConsumerServer {
	//messages produced by producerClient for consumerServer to publish to PubNub
    private static final String TASK_QUEUE_NAME = "task_queue_outbound_durable";
    private static final String publish_key = "demo";
    private static final String subscribe_key = "demo";
    
    public static void main(String[] argv) throws Exception {
        String channelName = "rabbitWorker";
    	Callback cb = new Callback(){
            @Override
            public void successCallback(String channel, Object message) {
                System.out.println(" [*] consumerServer : " + message);
            }
            @Override
            public void errorCallback(String channel, PubnubError error) {
            	System.out.println(" [!] consumerServer error:" + error);
            }
    	};
    	Pubnub pubnub = new Pubnub(publish_key, subscribe_key);
    	
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //configure message queues as durable
        boolean durable = true;

        channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
        System.out.println(" [*] consumerServer waiting for messages. To exit press CTRL+C");

        //dispatch messages fairly rather than round-robin by waitng for ack before sending next message
        //be careful because queue can fill up if all workers are busy
        int prefetchCount = 1;
        channel.basicQos(prefetchCount);

        //ensure that an eplicit ack is sent from worker before removing from the queue
        boolean autoAck = false;

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(TASK_QUEUE_NAME, autoAck, consumer);

        while (true) {
          QueueingConsumer.Delivery delivery = consumer.nextDelivery();
          String message = new String(delivery.getBody());
          
          System.out.println(" [x] consumerServer received '" + message + "'");
          
          doWork(message);

          pubnub.publish(channelName, message, cb);
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
      }

      private static void doWork(String task) throws InterruptedException {
        for (char ch: task.toCharArray()) {
          if (ch == '.') Thread.sleep(1000);
        }
      }
    }
