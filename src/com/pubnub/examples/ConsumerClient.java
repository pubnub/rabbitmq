package com.pubnub.examples;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/*
 * Note this class is nearly identical to Worker.java in tutorial 2 on the RabbitMQ site from
 * http://www.rabbitmq.com/tutorials/tutorial-two-java.html
 * You can download the source code directly from 
 * http://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/Worker.java
 * The only real difference is that we use 2 different RabbitMQ channels to avoid collisions.
 * We use one channel to produce messages to and another to consume messages from
 * So make sure you set the constant TASK_QUEUE_NAME to something other than 'task_queue', 
 * such as 'task_queue_inbound_durable'
*/

public class ConsumerClient {

	//messages subscribed from PubNub by producerServer for consumerClient to consumePubNub
	private static final String TASK_QUEUE_NAME = "task_queue_inbound_durable";
    
    public static void main(String[] argv) throws Exception {
    	
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //configure message queues as durable
        boolean durable = true;

        channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
        System.out.println(" [*]  consumerClient waiting for messages. To exit press CTRL+C");

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
          
          System.out.println(" [x] consumerClient received '" + message + "'");
          doWork(message);

          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
      }

      private static void doWork(String task) throws InterruptedException {
        for (char ch: task.toCharArray()) {
          if (ch == '.') Thread.sleep(1000);
        }
      }
    }
