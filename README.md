# SimpleKafka（Kafka客户端封装工具类）
一个基于Kafka客户端封装的工具，Kafka开发效率神器

## 特点：
1. 封装了常用的Kafka客户端操作，无需维护配置，无需初始化客户端，真正实现了一行代码调用
2. 将连接池的维护封装在工具类里面，多线程使用也无需维护客户端集合

## 使用方式：
只需要集成1个KafkaUtil.java文件即可，修改里面的kafka服务地址即可

## 接口介绍：
1. **select：** 所有的结果集查询接口，直接拼SQL查询语句一行代码调用函数即可，返回值就是直接SpringBoot可以响应的格式（当然也可以加工后返回），无需bean，无需新建mapper，支持分组查询，连接查询，子查询，组合查询（UNION），视图查询，各种统计类的查询也直接用这个接口即可，别名as什么SpringBoot响应的json字段就是什么（也就是LinkedHashMap的key）
2. **count:** 所有的单值计数查询的快捷接口，也可以用于分页接口的总数接口，直接返回就是一个long型的数字，无需任何解析和加工
3. **get：** 所有的单对象点查询的快捷接口，返回的直接是一个对象（函数返回LinkedHashMap，SpringBoot响应json对象）
4. **insert：** 所有的插入语句接口，返回值是成功行数
5. **insertForID：** 所有的插入语句，支持获得被插入数据的主键ID值的版本，返回值是成功行数，主键ID值在map.id
6. **update：** 所有的更新语句接口，返回值是成功行数
7. **delete：** 所有的删除语句接口，返回值是成功行数
8. **execute：** 所有的执行语句接口，返回值是成功行数，支持所有的DDL，DCL语句，常用于建库建表，建用户，赋权等操作
9. **executeBatch：** 支持批处理的接口，可以一次执行多条语句
10. **call：** 支持调用存储过程的接口，支持带结果集存储过程，也支持带OUT参数

## 接口列表：
1. **kafkaListTopics：** List kafkaListTopics()
2. **count:** List<String> kafkaConsumerGroups()
3. **get：** List<String> kafkaConsumerGroups(String topic)
4. **insert：** LinkedHashMap<String, Object> sendToKafka(String topic, String key, String value)
5. **insertForID：** void sendToKafkaAsync(String topic, String key, String value)
6. **update：** void sendToKafkaAsync(String topic, String key, String value,Callback callback)
7. **delete：** int delete(String sql,Object ...args)
8. **execute：** int execute(String sql,Object ...args)
9. **executeBatch：** int executeBatch(List<String> sql,Object ...args)
10. **call：** List<LinkedHashMap<String, Object>> call(String sql,Map map,Object ...args)

## 联系人：
有问题可以联系:zhangchuang@iie.ac.cn
