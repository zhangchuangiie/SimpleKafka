package com.example.demo.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class KafkaUtil {

    private static HashMap<String, KafkaConsumer<String, String>> kafkaConsumerMap = new HashMap<>();
    private static HashMap<String, KafkaProducer<String, String>> kafkaProducerMap = new HashMap<>();
    private static String brokerList = "";
    //topic列表
    public static List<String> kafkaListTopics() throws ExecutionException, InterruptedException {

        Properties props = new Properties();
        // 只需要提供一个或多个 broker 的 IP 和端口
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        // 创建 AdminClient 对象
        AdminClient client = KafkaAdminClient.create(props);
        // 获取 topic 列表
        Set topics = client.listTopics().names().get();
        System.out.println("=======================");
        System.out.println(topics);
        System.out.println("=======================");

        List<String> topicList = new ArrayList<String>();
        topicList.addAll(topics);

        return topicList;
    }

    //消费者列表
    public static List<String> kafkaConsumerGroups() throws ExecutionException, InterruptedException, TimeoutException {
        Properties props = new Properties();
        // 只需要提供一个或多个 broker 的 IP 和端口
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        // 创建 AdminClient 对象
        AdminClient client = KafkaAdminClient.create(props);
        List<String> allGroups = client.listConsumerGroups()
                .valid()
                .get(10, TimeUnit.SECONDS)
                .stream()
                .map(ConsumerGroupListing::groupId)
                .collect(Collectors.toList());

        //System.out.println(collection);
        System.out.println("=======================");
        System.out.println(JSON.toJSONString(allGroups));
        //System.out.println(JSON.toJSONString(filteredGroups));
        System.out.println("=======================");
        return allGroups;


    }

    //消费者列表,值得注意的是，上面这个函数无法获取非运行中的consumer group，即虽然一个group订阅了某topic，但是若它所有的consumer成员都关闭的话这个函数是不会返回该group的。
    public static List<String> kafkaConsumerGroups(String topic) throws ExecutionException, InterruptedException, TimeoutException {
        Properties props = new Properties();
        // 只需要提供一个或多个 broker 的 IP 和端口
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        // 创建 AdminClient 对象
        AdminClient client = KafkaAdminClient.create(props);
        List<String> allGroups = client.listConsumerGroups()
                .valid()
                .get(10, TimeUnit.SECONDS)
                .stream()
                .map(ConsumerGroupListing::groupId)
                .collect(Collectors.toList());

        Map<String, ConsumerGroupDescription> allGroupDetails =
                client.describeConsumerGroups(allGroups).all().get(10, TimeUnit.SECONDS);

        final List<String> filteredGroups = new ArrayList<>();
        allGroupDetails.entrySet().forEach(entry -> {
            String groupId = entry.getKey();
            ConsumerGroupDescription description = entry.getValue();
            boolean topicSubscribed = description.members().stream().map(MemberDescription::assignment)
                    .map(MemberAssignment::topicPartitions)
                    .map(tps -> tps.stream().map(TopicPartition::topic).collect(Collectors.toSet()))
                    .anyMatch(tps -> tps.contains(topic));
            if (topicSubscribed)
                filteredGroups.add(groupId);
        });
        //System.out.println(collection);
        System.out.println("=======================");
        System.out.println(JSON.toJSONString(allGroups));
        System.out.println(JSON.toJSONString(filteredGroups));
        System.out.println("=======================");
        return filteredGroups;


    }


    ///维护内部客户端池
    private static KafkaConsumer<String, String> getKafkaConsumer(String topic, String groupId) throws ExecutionException, InterruptedException {

        KafkaConsumer<String, String> kafkaConsumer = kafkaConsumerMap.get(topic+groupId);
        if (kafkaConsumer==null){
            //创建 kafka 消费者实例
            kafkaConsumer = getNewKafkaConsumer(topic, groupId);
            kafkaConsumerMap.put(topic+groupId,kafkaConsumer);
        }

        return kafkaConsumer;
    }
    ///维护内部客户端池
    private static KafkaProducer<String, String> getKafkaProducer() throws ExecutionException, InterruptedException {

        KafkaProducer<String, String> kafkaProducer = kafkaProducerMap.get("default");
        if (kafkaProducer==null){

            //创建一个生产者对象kafkaProducer
            kafkaProducer = getNewKafkaProducer();
            kafkaProducerMap.put("default",kafkaProducer);
        }

        return kafkaProducer;
    }
    ///正常不需要这个接口，本身都支持多线程，这个接口仅在想自己在多线程内初始化多个客户端时使用,依旧要受Kafka的“一个Partition只能被该Group里的一个Consumer线程消费”规则的限制
    public static KafkaConsumer<String, String> getNewKafkaConsumer(String topic, String groupId) throws ExecutionException, InterruptedException {

        //String groupId = "group1";
        //String topic = "hello-kafka";
        //配置消费者客户端
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        //properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "2");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        //properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1048576);
        //properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 1100000000);
        //消费消息-fetch.min.bytes:服务器为消费者获取请求返回的最小数据量。如果可用的数据不足，请求将等待大量的数据在回答请求之前累积。默认1B
        //消费消息-fetch.max.wait.ms:我们通过 fetch.min.bytes 告诉 Kafka，等到有足够的数据时才把它返回给消费者。而 fetch.max.wait.ms 则用于指定 broker 的等待时间，默认是 500ms。
        //消费消息-fetch.max.bytes:

        //一次调用poll()操作时返回的最大记录数，默认值为500
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 2000);

        //创建 kafka 消费者实例
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(properties);
        //订阅主题
        kafkaConsumer.subscribe(Collections.singletonList(topic));


        return kafkaConsumer;
    }
    ///正常不需要这个接口，本身都支持多线程，这个接口仅在想自己在多线程内初始化多个客户端时使用
    public static KafkaProducer<String, String> getNewKafkaProducer() throws ExecutionException, InterruptedException {

        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        //生产消息-ACK机制。0-意味着producer不等待broker同步完成的确认，继续发送下一条(批)信息；1-意味着producer要等待leader成功收到数据并得到确认，才发送下一条message；-1--意味着producer得到follwer确认，才发送下一条数据。
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        // 生产消息-生产者可以重发消息的次数，如果达到这个次数，生产者会放弃重试并返回错误。
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);
        //生产消息-能发送的单个消息的最大值，单位为B，默认为10M
        properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10485760);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 1);

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        //创建一个生产者对象kafkaProducer
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(properties);


        return kafkaProducer;
    }


    //生产数据到指定的topic,同步接口
    public static LinkedHashMap<String, Object> sendToKafka(String topic, String key, String value) throws ExecutionException, InterruptedException {
        long start=System.currentTimeMillis();   //获取开始时间
        KafkaProducer<String, String> kafkaProducer = getKafkaProducer();
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic,key,value);
        RecordMetadata recordMetadata = kafkaProducer.send(producerRecord).get();

        System.out.println("recordMetadata = " + recordMetadata);
        System.out.println("topic=" + recordMetadata.topic());
        System.out.println("partition=" + recordMetadata.partition());
        System.out.println("offset=" + recordMetadata.offset());

        LinkedHashMap<String, Object> recordMeta = new LinkedHashMap<String, Object>();

        recordMeta.put("topic",recordMetadata.topic());
        recordMeta.put("partition",recordMetadata.partition());
        recordMeta.put("offset",recordMetadata.offset());
        long end=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(end-start)+"ms");
        return recordMeta;
    }


    //生产数据到指定的topic，异步接口，默认回调
    public static void sendToKafkaAsync(String topic, String key, String value) throws ExecutionException, InterruptedException {
        long start=System.currentTimeMillis();   //获取开始时间
        KafkaProducer<String, String> kafkaProducer = getKafkaProducer();
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic,key,value);

        Callback callback = new Callback() {
            long start=System.currentTimeMillis();   //获取开始时间
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception
                    exception) {
                if (exception == null) {
                    // 消息发送成功
                    System.out.println("消息发送成功");
                    long end=System.currentTimeMillis(); //获取结束时间
                    System.out.println("回调等待时间： "+(end-start)+"ms");
                    System.out.println(TimeUtil.getCurrentDateStringMillisecond());
                    System.out.println("recordMetadata = " + recordMetadata);
                    System.out.println("topic=" + recordMetadata.topic());
                    System.out.println("partition=" + recordMetadata.partition());
                    System.out.println("offset=" + recordMetadata.offset());
                } else {
                    System.out.println("消息发送失败");
                    // 消息发送失败，需要重新发送
                }
            }
        };
        Future<RecordMetadata> recordMetadata = kafkaProducer.send(producerRecord,callback);

        long end=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(end-start)+"ms");
        return;
    }


    //生产数据到指定的topic，异步接口，自定义回调
    public static void sendToKafkaAsync(String topic, String key, String value,Callback callback) throws ExecutionException, InterruptedException {
        long start=System.currentTimeMillis();   //获取开始时间
        KafkaProducer<String, String> kafkaProducer = getKafkaProducer();
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic,key,value);
        Future<RecordMetadata> recordMetadata = kafkaProducer.send(producerRecord,callback);
        long end=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(end-start)+"ms");
        return;
    }


    //按groupId消费指定topic的数据
    public static ArrayList<LinkedHashMap<String, Object>> recvFromKafka(String topic, String groupId) throws ExecutionException, InterruptedException {

        KafkaConsumer<String, String> kafkaConsumer = getKafkaConsumer(topic, groupId);
        //用于保存消息的list
        ArrayList buffer = new ArrayList<>();

        long start=System.currentTimeMillis();   //获取开始时间
        ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(5000));
        kafkaConsumer.commitAsync();
        long end=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(end-start)+"ms");
        //System.out.println("i = " + i);
        System.out.println("consumerRecords = " + consumerRecords.count());

        for (ConsumerRecord<String, String> record : consumerRecords) {
            LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

            data.put("订阅主题", record.topic());
            data.put("消息键值", record.key());
            data.put("消息内容", record.value());
            data.put("消息内容的偏移量", record.offset());
            data.put("消息内容分区", record.partition());
            buffer.add(data);
            System.out.println("topic=" + record.topic());
            System.out.println("key=" + record.key());
            System.out.println("value=" + record.value());
            System.out.println("partition=" + record.partition());
            System.out.println("offset=" + record.offset());
        }
        return buffer;
    }


    //消费指定topic指定partition对应的offset数据
    public static ArrayList<LinkedHashMap<String, Object>> recvFromKafkaByOffset(String topic, String groupId,int partition,long offset) throws ExecutionException, InterruptedException {

        KafkaConsumer<String, String> kafkaConsumer = getKafkaConsumer(topic, groupId);
        //用于保存消息的list
        ArrayList buffer = new ArrayList<>();

        long start=System.currentTimeMillis();   //获取开始时间
        // 指定位置开始消费
        Set<TopicPartition> assignment= new HashSet<>();
        while (assignment.size() == 0) {
            kafkaConsumer.poll(Duration.ofSeconds(1));
            // 获取消费者分区分配信息（有了分区分配信息才能开始消费）
            assignment = kafkaConsumer.assignment();
        }
        // 遍历所有分区，并指定 offset 从 100 的位置开始消费
        TopicPartition topicPartition = new TopicPartition(topic,partition);
        kafkaConsumer.seek(topicPartition,offset); // 指定offset

        ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(5000));
        kafkaConsumer.commitAsync();
        long end=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(end-start)+"ms");
        //System.out.println("i = " + i);
        System.out.println("consumerRecords = " + consumerRecords.count());

        for (ConsumerRecord<String, String> record : consumerRecords) {
            LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
            data.put("订阅主题", record.topic());
            data.put("消息键值", record.key());
            data.put("消息内容", record.value());
            data.put("消息内容的偏移量", record.offset());
            data.put("消息内容分区", record.partition());
            buffer.add(data);
            System.out.println("topic=" + record.topic());
            System.out.println("key=" + record.key());
            System.out.println("value=" + record.value());
            System.out.println("partition=" + record.partition());
            System.out.println("offset=" + record.offset());
        }
        return buffer;
    }

    //消费指定topic指定partition对应的timestamp以后的数据
    public static ArrayList<LinkedHashMap<String, Object>> recvFromKafkaByTimestamp(String topic, String groupId,int partition,long timestamp) throws ExecutionException, InterruptedException {

        KafkaConsumer<String, String> kafkaConsumer = getKafkaConsumer(topic, groupId);
        //用于保存消息的list
        ArrayList buffer = new ArrayList<>();

        long start=System.currentTimeMillis();   //获取开始时间
        // 指定位置开始消费
        Set<TopicPartition> assignment= new HashSet<>();
        while (assignment.size() == 0) {
            kafkaConsumer.poll(Duration.ofSeconds(1));
            // 获取消费者分区分配信息（有了分区分配信息才能开始消费）
            assignment = kafkaConsumer.assignment();
        }
        // 遍历所有分区，并指定 offset 从 100 的位置开始消费
        TopicPartition topicPartition = new TopicPartition(topic,partition);

        Map<TopicPartition, Long> query = new HashMap<>();//构造offsetsForTimes参数，通过时间戳找到offset
        query.put(topicPartition, timestamp);
        Map<TopicPartition, OffsetAndTimestamp> result = kafkaConsumer.offsetsForTimes(query);
        long offset = result.get(topicPartition).offset();

        kafkaConsumer.seek(topicPartition,offset); // 指定offset

        ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(5000));
        kafkaConsumer.commitAsync();
        long end=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(end-start)+"ms");
        //System.out.println("i = " + i);
        System.out.println("consumerRecords = " + consumerRecords.count());

        for (ConsumerRecord<String, String> record : consumerRecords) {
            LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
            data.put("订阅主题", record.topic());
            data.put("消息键值", record.key());
            data.put("消息内容", record.value());
            data.put("消息内容的偏移量", record.offset());
            data.put("消息内容分区", record.partition());
            buffer.add(data);
            System.out.println("topic=" + record.topic());
            System.out.println("key=" + record.key());
            System.out.println("value=" + record.value());
            System.out.println("partition=" + record.partition());
            System.out.println("offset=" + record.offset());
        }
        return buffer;
    }

    //重置指定topic的offset到对应的timestamp
    public static boolean resetOffsetToTimestamp(String topic, String groupId, long timestamp) throws ExecutionException, InterruptedException {

        KafkaConsumer<String, String> kafkaConsumer = getKafkaConsumer(topic, groupId);
        //用于保存消息的list
        ArrayList buffer = new ArrayList<>();

        long start=System.currentTimeMillis();   //获取开始时间
        // 指定位置开始消费
        Set<TopicPartition> assignment= new HashSet<>();
        while (assignment.size() == 0) {
            kafkaConsumer.poll(Duration.ofSeconds(1));
            // 获取消费者分区分配信息（有了分区分配信息才能开始消费）
            assignment = kafkaConsumer.assignment();
        }
        // 遍历所有分区，并指定 offset 从 100 的位置开始消费
        for (TopicPartition topicPartition : assignment) {
            Map<TopicPartition, Long> query = new HashMap<>();//构造offsetsForTimes参数，通过时间戳找到offset
            query.put(topicPartition, timestamp);
            Map<TopicPartition, OffsetAndTimestamp> result = kafkaConsumer.offsetsForTimes(query);
            if (result.get(topicPartition) !=null){
                long offset = result.get(topicPartition).offset();
                kafkaConsumer.seek(topicPartition,offset); // 指定offset
                kafkaConsumer.commitAsync();
            }

        }
        long end=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(end-start)+"ms");

        return true;
    }
    //重置指定topic的offset到最早
    public static boolean resetOffsetToEarliest(String topic, String groupId) throws ExecutionException, InterruptedException {

        KafkaConsumer<String, String> kafkaConsumer = getKafkaConsumer(topic, groupId);

        long start=System.currentTimeMillis();   //获取开始时间
        // 指定位置开始消费
        Set<TopicPartition> assignment= new HashSet<>();
        while (assignment.size() == 0) {
            kafkaConsumer.poll(Duration.ofSeconds(1));
            // 获取消费者分区分配信息（有了分区分配信息才能开始消费）
            assignment = kafkaConsumer.assignment();
        }
        // 遍历所有分区，并指定 offset 从 100 的位置开始消费
        for (TopicPartition topicPartition : assignment) {
            Map<TopicPartition, Long> query = new HashMap<>();//构造offsetsForTimes参数，通过时间戳找到offset
            long begin = kafkaConsumer.beginningOffsets(Arrays.asList(topicPartition)).get(topicPartition);
            kafkaConsumer.seek(topicPartition,begin); // 指定offset
            kafkaConsumer.commitAsync();
        }
        long end=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(end-start)+"ms");

        return true;
    }
    //重置指定topic的offset到最晚，一般在跳过测试脏数据时候使用
    public static boolean resetOffsetToLatest(String topic, String groupId) throws ExecutionException, InterruptedException {

        KafkaConsumer<String, String> kafkaConsumer = getKafkaConsumer(topic, groupId);

        long start=System.currentTimeMillis();   //获取开始时间
        // 指定位置开始消费
        Set<TopicPartition> assignment= new HashSet<>();
        while (assignment.size() == 0) {
            kafkaConsumer.poll(Duration.ofSeconds(1));
            // 获取消费者分区分配信息（有了分区分配信息才能开始消费）
            assignment = kafkaConsumer.assignment();
        }
        // 遍历所有分区，并指定 offset 从 100 的位置开始消费
        for (TopicPartition topicPartition : assignment) {
            Map<TopicPartition, Long> query = new HashMap<>();//构造offsetsForTimes参数，通过时间戳找到offset
            long end = kafkaConsumer.endOffsets(Arrays.asList(topicPartition)).get(topicPartition);
            kafkaConsumer.seek(topicPartition,end); // 指定offset
            kafkaConsumer.commitAsync();
        }
        long end=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(end-start)+"ms");

        return true;
    }


    ///获取当前消费偏移量情况
    private static List<LinkedHashMap<String, Object>> consumerPositions(String topic, String groupId) throws ExecutionException, InterruptedException{
        KafkaConsumer<String, String> kafkaConsumer = getKafkaConsumer(topic, groupId);

        List<PartitionInfo> partitionInfos = kafkaConsumer.partitionsFor(topic);
        System.out.println("Get the partition info as below:");
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        for (PartitionInfo partitionInfo : partitionInfos) {
            LinkedHashMap<String, Object> offsetMeta = new LinkedHashMap<String, Object>();
            TopicPartition topicPartition = new TopicPartition(topic, partitionInfo.partition());
            try {
                // 5.通过Consumer.endOffsets(Collections<TopicPartition>)方法获取
                // 指定TopicPartition对应的lastOffset
                long begin = kafkaConsumer.beginningOffsets(Arrays.asList(topicPartition)).get(topicPartition);
                long end = kafkaConsumer.endOffsets(Arrays.asList(topicPartition)).get(topicPartition);
                Set<TopicPartition> partitions = new HashSet<TopicPartition>();
                partitions.add(topicPartition);
                long current = kafkaConsumer.committed(partitions).get(topicPartition).offset();
                long partition = topicPartition.partition();
//                long lag = end - current;
//                long offset = current -begin;
//                long size = end - begin;
                System.out.println("partition = " + partition);
                System.out.println("begin = " + begin);
                System.out.println("end = " + end);
                System.out.println("current = " + current);
                offsetMeta.put("partition",partition);
                offsetMeta.put("begin",begin);
                offsetMeta.put("end",end);
                offsetMeta.put("current",current);
                result.add(offsetMeta);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return result;
    }





    public static void main(String[] args) throws ExecutionException, InterruptedException,TimeoutException {
        long start=System.currentTimeMillis();   //获取开始时间

        List list = KafkaUtil.kafkaListTopics();



        //resetOffsetToEarliest("RULEa93304e6d844000", "group1");
        //LinkedHashMap<String, Object> recordMeta = sendToKafka("RULEa93304e6d844000","222","aaaa");
        //JSONObject object = JSONUtil.parseObj(recordMeta);
        //System.out.println("object.toJSONString(4) = " + object.toJSONString(4));
//        sendToKafka("RULEa93304e6d844000","333","aaaa");

        //System.out.println("kafkaListTopics() = " + kafkaListTopics());

        //ArrayList buffer = recvFromKafka("RULEa93304e6d844000", "group1");
        //recvFromKafka("RULEa93304e6d844000", "group2");
        //recvFromKafka("RULEa93304e6d844000", "group3");

        //kafkaConsumerGroups("RULEa93304e6d844000");
        //kafkaConsumerGroups();
        sendToKafka("RULEa93304e6d844000", "222", "aaaa");

        for (int i = 0; i < 100; i++) {
            sendToKafkaAsync("RULEa93304e6d844000", "222", "aaaa");
            //sendToKafka("RULEa93304e6d844000", "333", "aaaa");
        }

        TimeUnit.DAYS.sleep(1);

 /*       for (int i = 0; i < 100; i++) {

            try {
                sendToKafka("RULEa93304e6d844000","222","aaaa");
               sendToKafka("RULEa93304e6d844000","333","aaaa");
//                sendToKafka("RULEa93304e6d844000","222","aaaa");
//                sendToKafka("RULEa93304e6d844000","333","aaaa");
//                ArrayList buffer1 = recvFromKafka("RULEa93304e6d844000", "group1");
//                System.out.println("buffer1.size() = " + buffer1.size());
//                TimeUnit.SECONDS.sleep(30); // 休眠 1s
                //resetOffsetToTimestamp("RULEa93304e6d844000", "group1",1661853600000L);
                resetOffsetToEarliest("RULEa93304e6d844000", "group1");

                //ArrayList buffer1 = recvFromKafkaByTimestamp("RULEa93304e6d844000", "group1",1,1661853600000L);
                //recvFromKafkaByOffset("RULEa93304e6d844000", "group1",1,10);
                //System.out.println("buffer1.size() = " + buffer1.size());
                TimeUnit.SECONDS.sleep(30); // 休眠 1s

                List<LinkedHashMap<String, Object>> result = consumerPositions("RULEa93304e6d844000", "group1");
                JSONArray array = JSONUtil.parseArray(result);
                System.out.println("array.toJSONString(4) = " + array.toJSONString(4));
                //TimeUnit.DAYS.sleep(1); // 休眠 1 天
            } catch (Exception e) {
                e.printStackTrace();
            }

        }*/
        
        
        
        
        
        
        //long end=System.currentTimeMillis(); //获取结束时间
        //System.out.println("程序运行时间： "+(end-start)+"ms");

    }
}
