package tester;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;

public class KafkaPractice {

	private static final String LOCATION = "127.0.0.1:9092";

	private static final String CREATE_TOPIC = "topic";

	private static final String PRODUCER_SEND = "send";

	private static final String CONSUMER_RECV = "recv";

	public static void main(String[] args) {

		if (args.length > 0) {

			String cmd = args[0];

			switch (cmd) {
			case CREATE_TOPIC:
				createTopic();
				break;
			case PRODUCER_SEND:
				producerSendMessage();
				break;
			case CONSUMER_RECV:
				consumerRecvMessage();
				break;

			default:
				break;
			}
		}

	}

	private static void createTopic() {
		// 创建topic
		Properties props = new Properties();
		props.put("bootstrap.servers", LOCATION);
		
		AdminClient adminClient = AdminClient.create(props);
		ArrayList<NewTopic> topics = new ArrayList<NewTopic>();
		NewTopic newTopic = new NewTopic("topic-test", 1, (short) 1);
		topics.add(newTopic);
		CreateTopicsResult result = adminClient.createTopics(topics);
		try {
			result.all().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	private static void producerSendMessage() {
		Properties props = new Properties();
		props.put("bootstrap.servers", LOCATION);
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		Producer<String, String> producer = new KafkaProducer<String, String>(props);
		int i =0;
		
		while(true){
			producer.send(new ProducerRecord<String, String>("topic-test", Integer.toString(i), Integer.toString(i)));
			i++;
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(i >= 2000){
				break;
			}
		}

		producer.close();
	}

	private static void consumerRecvMessage() {
		Properties props = new Properties();
		props.put("bootstrap.servers", LOCATION);
		props.put("group.id", "test");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		final KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Arrays.asList("topic-test"), new ConsumerRebalanceListener() {
			public void onPartitionsRevoked(Collection<TopicPartition> collection) {
			}

			public void onPartitionsAssigned(Collection<TopicPartition> collection) {
				// 将偏移设置到最开始
				consumer.seekToBeginning(collection);
			}
		});
		while (true) {
			Duration timeout = Duration.of(100, ChronoUnit.MILLIS);
			ConsumerRecords<String, String> records = consumer.poll(timeout);
			for (ConsumerRecord<String, String> record : records)
				System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
		}
	}

}
