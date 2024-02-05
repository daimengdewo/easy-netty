package com.awen.energy.protocol.message;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
public class ChannelMap {
    /**
     * 存放客户端标识ID（消息ID）与channel的对应关系
     */
    //全局变量
    Map<String, ChannelHandlerContext> contextHashMap = new HashMap<>();

    //获取全局变量信息
    public ChannelHandlerContext getContext(String key) {
        return contextHashMap.get(key);
    }

    //更新全局变量信息
    public void addContext(String key, ChannelHandlerContext ctx) {
        contextHashMap.put(key, ctx);
    }

    //清空全局变量信息
    public void deleteContext(String key) {
        contextHashMap.remove(key);
    }
}