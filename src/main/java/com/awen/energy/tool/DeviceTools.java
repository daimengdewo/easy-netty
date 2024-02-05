package com.awen.energy.tool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DeviceTools {

    /**
     * 解析上报明文
     */
    public Map<String, String> analysisEquipment(String content) {
        System.out.println("接收内容：" + content + "\n");
        System.out.println("---------------------------------------\n");
        //创建哈希表
        Map<String, String> device = new HashMap<>();
        if (content.contains("msg:")) {
            //去掉头部
            String replace = content.replaceFirst("msg:", "");
            //解析成数组
            String[] ct = replace.split(",");
            for (String str : ct) {
                String[] element = str.split(":");
                device.put(element[0], element[1]);
            }
        } else {
            device.put("cmd", null);
        }
        return device;
    }

    /**
     * 解析上报明文
     */
    public Map<String, String> CwEquipment(String content) {
        String[] pairs = content.split("&");
        Map<String, String> keyValuePairs = new HashMap<>();

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = keyValue[0];
            String value = keyValue[1];
            keyValuePairs.put(key, value);
        }
        return keyValuePairs;
    }
}
