# SimpleKafka（Kafka客户端封装工具类）
一个基于Kafka客户端封装的工具，Kafka开发效率神器

## 特点：
1. 封装了常用的Kafka客户端操作，无需维护配置，无需初始化客户端，真正实现了一行代码调用
2. 将连接池的维护封装在工具类里面，多线程使用也无需维护客户端集合

## 使用方式：
只需要集成1个KafkaUtil.java文件即可，修改里面的kafka服务地址即可

## 典型示例：
1. **同步生产:** LinkedHashMap<String, Object> recordMeta = KafkaUtil.sendToKafka("RULEa93304e6d844000","222","aaaa");
2. **异步生产:** KafkaUtil.sendToKafkaAsync("RULEa93304e6d844000", "222", "aaaa");
3. **消费数据:** ArrayList<LinkedHashMap<String, Object>> buffer = KafkaUtil.recvFromKafka("RULEa93304e6d844000", "group1");
4. **重置偏移:** KafkaUtil.resetOffsetToEarliest("RULEa93304e6d844000", "group1");


## 接口介绍：
1. **kafkaListTopics：** topic列表
2. **createTopic:** topic创建
3. **delTopic:** topic删除
4. **partitionsTopic:** topic的分区列表，分区和副本数
5. **delGroupId:** 删除groupId
6. **descCluster:** 集群的节点列表
7. **kafkaConsumerGroups：** 消费者列表
8. **kafkaConsumerGroups：** 指定topic的活跃消费者列表
9. **sendToKafka：** 生产数据到指定的topic,同步接口
10. **sendToKafkaAsync：** 生产数据到指定的topic，异步接口，默认回调
11. **sendToKafkaAsync：** 生产数据到指定的topic，异步接口，自定义回调
12. **recvFromKafka：** 按groupId消费指定topic的数据
13. **recvFromKafkaByOffset：** 消费指定topic指定partition对应的offset数据
14. **recvFromKafkaByTimestamp：** 消费指定topic指定partition对应的timestamp以后的数据
15. **resetOffsetToTimestamp：** 重置指定topic的offset到对应的timestamp
16. **resetOffsetToEarliest：** 重置指定topic的offset到最早
17. **resetOffsetToLatest：** 重置指定topic的offset到最晚，一般在跳过测试脏数据时候使用
18. **consumerPositions：** 获取当前消费偏移量情况
19. **topicSize：** 获取指定topic数据量详情情况 {"partition": 0,"begin": 65,"end": 65,"size": 0}
20. **topicSizeAll：** 获取所有topic数据量详情情况
21. **topicSizeStatistics：** 获取指定topic数据量统计{"partitionNum":5452,"dataNum":41570647}
22. **topicSizeStatisticsALL：** 获取所有topic数据量统计{"topicNum":2550,"partitionNum":5452,"dataNum":41570647}

## 接口列表：
1. **kafkaListTopics:** List<String> kafkaListTopics()
2. **createTopic:** void createTopic(String topic)
3. **delTopic:** void delTopic(String topic)
4. **partitionsTopic:** List<String> partitionsTopic(String topic)
5. **delGroupId:** void delGroupId(String groupId)
6. **descCluster:** List<String> descCluster()
7. **kafkaConsumerGroups:** List<String> kafkaConsumerGroups()
8. **kafkaConsumerGroups：** List<String> kafkaConsumerGroups(String topic)
9. **sendToKafka：** LinkedHashMap<String, Object> sendToKafka(String topic, String key, String value)
10. **sendToKafkaAsync：** void sendToKafkaAsync(String topic, String key, String value)
11. **sendToKafkaAsync：** void sendToKafkaAsync(String topic, String key, String value,Callback callback)
12. **recvFromKafka：** ArrayList<LinkedHashMap<String, Object>> recvFromKafka(String topic, String groupId)
13. **recvFromKafkaByOffset：** ArrayList<LinkedHashMap<String, Object>> recvFromKafkaByOffset(String topic, String groupId,int partition,long offset)
14. **recvFromKafkaByTimestamp：** ArrayList<LinkedHashMap<String, Object>> recvFromKafkaByTimestamp(String topic, String groupId,int partition,long timestamp)
15. **resetOffsetToTimestamp：** boolean resetOffsetToTimestamp(String topic, String groupId, long timestamp)
16. **resetOffsetToEarliest：** boolean resetOffsetToEarliest(String topic, String groupId)
17. **resetOffsetToLatest：** boolean resetOffsetToLatest(String topic, String groupId)
18. **consumerPositions：** List<LinkedHashMap<String, Object>> consumerPositions(String topic, String groupId)
19. **topicSize：** List<LinkedHashMap<String, Object>> topicSize(String topic)
20. **topicSizeAll：** LinkedHashMap<String, Object> topicSizeAll()
21. **topicSizeStatistics：** LinkedHashMap<String, Object> topicSizeStatistics(String topic)
22. **topicSizeStatisticsALL：** LinkedHashMap<String, Object> topicSizeStatisticsALL()

## 联系人：
有问题可以联系:zhangchuang@iie.ac.cn
