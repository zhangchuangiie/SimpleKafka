package com.example.demo.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.commom.RespValue;
import com.example.demo.util.KafkaUtil;
import com.example.demo.util.OgnlUtil;
import lombok.extern.slf4j.Slf4j;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping(value="/data",method = {RequestMethod.GET,RequestMethod.POST})
public class APIKafka {

    private Object getValue(Object expression, JSONObject keyObject) throws OgnlException {
        OgnlContext oc = new OgnlContext();
        oc.setRoot(keyObject);
        Object object = Ognl.getValue(expression, oc, oc.getRoot());
        return object;
    }


    @PostMapping("/sendData")
    public RespValue sendMessage(@RequestParam("key")String key,
                                 @RequestParam("value")String value) throws ExecutionException, InterruptedException {
        //同步发送
        //KafkaUtil.sendToKafka("producer",key,value);
        //异步发送
        KafkaUtil.sendToKafkaAsync("producer",key,value);
        return new RespValue(0,"插入成功","");
    }

    @PostMapping("/recvData")
    public RespValue recvData(@RequestParam(name="filter",required = false)String filter,
                              @RequestParam(name="groupId",required = false)String groupId) throws ExecutionException, InterruptedException, OgnlException {
        if(filter == null){filter = "true";}
        if(groupId == null){groupId = "guest";}

        ArrayList<LinkedHashMap<String, Object>> buffer = new ArrayList<LinkedHashMap<String, Object>>();
        ArrayList<LinkedHashMap<String, Object>> bufferTmp  = new ArrayList<LinkedHashMap<String, Object>>();
        try {
            bufferTmp = KafkaUtil.recvFromKafka("producer", groupId);
        } catch (ConcurrentModificationException e) {
            return new RespValue(0, "请使用单线程消费", buffer);
        }
        Object tree = Ognl.parseExpression(filter);
        
        long start = System.currentTimeMillis();   //获取开始时间
        for (int i = 0; i < bufferTmp.size(); i++) {

            LinkedHashMap<String, Object> o = bufferTmp.get(i);
            String key = (String) o.get("key");
            System.out.println("key = " + key);
            if(key.contains("=")) continue;
            JSONObject keyObject = JSON.parseObject(key);

            Boolean object = false;
            try {
                object = (Boolean) getValue(tree,keyObject);
            }catch (Exception e){
                System.out.println("e = " + e);
                System.out.println("标签不含表达式参数，跳过");
                continue;
            }
            System.out.println("object = " + object);
            System.out.println("object = " + object.getClass());
            if(object){
                buffer.add(o);
            }
        }
        long end=System.currentTimeMillis(); //获取结束时间
        System.out.println("filter程序运行时间： "+(end-start)+"ms");

        return new RespValue(0,"消费成功",buffer);
    }

    @PostMapping("/resetOffsetToEarliest")
    public RespValue resetOffsetToEarliest(@RequestParam("groupId")String groupId) throws ExecutionException, InterruptedException {
        KafkaUtil.resetOffsetToEarliest("producer", groupId);
        return new RespValue(0,"修改成功","");
    }

    @PostMapping("/consumerPositions")
    public RespValue consumerPositions(@RequestParam("groupId")String groupId) throws ExecutionException, InterruptedException {
        LinkedHashMap<String, Object> oo= KafkaUtil.consumerPositions("producer", groupId);
        oo.remove("positions");

        return new RespValue(0,"当前数据情况",oo);
    }


}

