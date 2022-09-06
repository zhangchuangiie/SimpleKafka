# SimpleKafka（Kafka客户端封装工具类）
一个基于Kafka客户端封装的工具，Kafka开发效率神器

## 特点：
1. 封装了常用的Kafka客户端操作，无需维护配置，无需初始化客户端，真正实现了一行代码调用
2. 将连接池的维护封装在工具类里面，多线程使用也无需维护客户端集合

## 使用方式：
只需要集成1个KafkaUtil.java文件即可，修改里面的kafka服务地址即可

## 接口介绍：
1. **kafkaListTopics：** topic列表
2. **kafkaConsumerGroups:** 消费者列表
3. **kafkaConsumerGroups：**指定topic的活跃消费者列表
4. **sendToKafka：** 生产数据到指定的topic,同步接口
5. **sendToKafkaAsync：** 生产数据到指定的topic，异步接口，默认回调
6. **sendToKafkaAsync：** 生产数据到指定的topic，异步接口，自定义回调
7. **recvFromKafka：** 按groupId消费指定topic的数据
8. **recvFromKafkaByOffset：** 消费指定topic指定partition对应的offset数据
9. **recvFromKafkaByTimestamp：** 消费指定topic指定partition对应的timestamp以后的数据
10. **resetOffsetToTimestamp：** 重置指定topic的offset到对应的timestamp
11. **resetOffsetToEarliest：** 重置指定topic的offset到最早
12. **resetOffsetToLatest：** 重置指定topic的offset到最晚，一般在跳过测试脏数据时候使用
13. **consumerPositions：** 获取当前消费偏移量情况

## 接口列表：
1. **kafkaListTopics：** List<String> kafkaListTopics()
2. **kafkaConsumerGroups:** List<String> kafkaConsumerGroups()
3. **kafkaConsumerGroups：** List<String> kafkaConsumerGroups(String topic)
4. **sendToKafka：** LinkedHashMap<String, Object> sendToKafka(String topic, String key, String value)
5. **sendToKafkaAsync：** void sendToKafkaAsync(String topic, String key, String value)
6. **sendToKafkaAsync：** void sendToKafkaAsync(String topic, String key, String value,Callback callback)
7. **recvFromKafka：** ArrayList<LinkedHashMap<String, Object>> recvFromKafka(String topic, String groupId)
8. **recvFromKafkaByOffset：** ArrayList<LinkedHashMap<String, Object>> recvFromKafkaByOffset(String topic, String groupId,int partition,long offset)
9. **recvFromKafkaByTimestamp：** ArrayList<LinkedHashMap<String, Object>> recvFromKafkaByTimestamp(String topic, String groupId,int partition,long timestamp)
10. **resetOffsetToTimestamp：** boolean resetOffsetToTimestamp(String topic, String groupId, long timestamp)
11. **resetOffsetToEarliest：** boolean resetOffsetToEarliest(String topic, String groupId)
12. **resetOffsetToLatest：** boolean resetOffsetToLatest(String topic, String groupId)
13. **consumerPositions：** List<LinkedHashMap<String, Object>> consumerPositions(String topic, String groupId)

## 联系人：
有问题可以联系:zhangchuang@iie.ac.cn
