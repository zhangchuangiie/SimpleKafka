package com.example.demo.client;

import cn.hutool.http.HttpUtil;
import java.util.HashMap;

public class ClientKafka {

    public static void main(String[] args) {
        try {

//            HashMap<String,Object> paramMap1=new HashMap<>();
//            paramMap1.put("key","{\"a\":1,\"b\":2}");
//            paramMap1.put("value","aaa");
//            String result1=HttpUtil.post("http://localhost:8080/api/data/sendData",paramMap1);
//            System.out.println("result1 = " + result1);
//
//            HashMap<String,Object> paramMap2=new HashMap<>();
//            paramMap2.put("key","{\"a\":2,\"b\":2}");
//            paramMap2.put("value","aaa");
//            String result2=HttpUtil.post("http://localhost:8080/api/data/sendData",paramMap2);
//            System.out.println("result2 = " + result2);
//
//            HashMap<String,Object> paramMap3=new HashMap<>();
//            paramMap3.put("key","{\"a\":3,\"b\":2}");
//            paramMap3.put("value","aaa");
//            String result3=HttpUtil.post("http://localhost:8080/api/data/sendData",paramMap3);
//            System.out.println("result3 = " + result3);
//
//            HashMap<String,Object> paramMap4=new HashMap<>();
//            paramMap4.put("filter","a<b");
//            paramMap4.put("groupId","group1");
//            String result4=HttpUtil.post("http://localhost:8080/api/data/recvData",paramMap4);
//            System.out.println("result4 = " + result4);
//
//            HashMap<String,Object> paramMap5=new HashMap<>();
//            paramMap5.put("filter","a==b");
//            paramMap5.put("groupId","group2");
//            String result5=HttpUtil.post("http://localhost:8080/api/data/recvData",paramMap5);
//            System.out.println("result5 = " + result5);
//
//            HashMap<String,Object> paramMap6=new HashMap<>();
//            paramMap6.put("filter","a>b");
//            paramMap6.put("groupId","group3");
//            String result6=HttpUtil.post("http://localhost:8080/api/data/recvData",paramMap6);
//            System.out.println("result6 = " + result6);
//
//            HashMap<String,Object> paramMap7=new HashMap<>();
//            paramMap7.put("filter","a==1 && b==2");
//            paramMap7.put("groupId","group4");
//            String result7=HttpUtil.post("http://localhost:8080/api/data/recvData",paramMap7);
//            System.out.println("result7 = " + result7);


//            HashMap<String,Object> paramMap1=new HashMap<>();
//            paramMap1.put("key","{\"a\":1,\"b\":2,\"c\":\"aaa\"}");
//            paramMap1.put("value","aaa");
//            String result1=HttpUtil.post("http://localhost:8080/api/data/sendData",paramMap1);
//            System.out.println("result1 = " + result1);

//            HashMap<String,Object> paramMap1=new HashMap<>();
//            paramMap1.put("key","{\"c\":\"aaa\"}");
//            paramMap1.put("value","aaa");
//            String result1=HttpUtil.post("http://localhost:8080/api/data/sendData",paramMap1);
//            System.out.println("result1 = " + result1);
//
//            HashMap<String,Object> paramMap7=new HashMap<>();
//            paramMap7.put("filter","c=='aaa'");
//            paramMap7.put("groupId","group3");
//            String result7=HttpUtil.post("http://localhost:8080/api/data/recvData",paramMap7);
//            System.out.println("result7 = " + result7);
//            HashMap<String,Object> paramMap2=new HashMap<>();
//            paramMap2.put("groupId","group4");
//            String result2=HttpUtil.post("http://localhost:8080/api/data/resetOffsetToEarliest",paramMap2);
//            System.out.println("result2 = " + result2);
//
//            HashMap<String,Object> paramMap8=new HashMap<>();
//            paramMap8.put("groupId","group4");
//            String result8=HttpUtil.post("http://localhost:8080/api/data/consumerPositions",paramMap8);
//            System.out.println("result8 = " + result8);

//            HashMap<String,Object> paramMap7=new HashMap<>();
//            paramMap7.put("groupId","group4");
//            String result7=HttpUtil.post("http://localhost:8080/api/data/recvData",paramMap7);
//            System.out.println("result7 = " + result7);


            HashMap<String,Object> paramMap2=new HashMap<>();
            paramMap2.put("key","{\"other\":\"label1_22\"}");
            paramMap2.put("value","aaa");
            String result2=HttpUtil.post("http://localhost:8080/api/data/sendData",paramMap2);
            System.out.println("result2 = " + result2);


            HashMap<String,Object> paramMap3=new HashMap<>();
            paramMap3.put("groupId","group4");
            String result3=HttpUtil.post("http://localhost:8080/api/data/resetOffsetToEarliest",paramMap3);
            System.out.println("result3 = " + result3);

            HashMap<String,Object> paramMap8=new HashMap<>();
            //paramMap8.put("filter","!other.contains(\"label1_\")");
            paramMap8.put("filter","1==1");
            paramMap8.put("groupId","group4");
            String result8=HttpUtil.post("http://localhost:8080/api/data/recvData",paramMap8);
            System.out.println("result8 = " + result8);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
