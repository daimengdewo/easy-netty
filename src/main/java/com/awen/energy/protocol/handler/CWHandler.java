package com.awen.energy.protocol.handler;

import com.awen.energy.dto.NettyChannelData;
import com.awen.energy.protocol.message.ChannelMap;
import com.awen.energy.tool.DeviceTools;
import io.netty.channel.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@ChannelHandler.Sharable
public class CWHandler extends SimpleChannelInboundHandler<String> {

    // 外部注入的频道列表
    private ChannelMap channelMap;
    // 内部创建的设备工具类
    private DeviceTools deviceTools = new DeviceTools();

    /**
     * 有客户端与服务器发生连接时执行此方法
     * 1.打印提示信息
     * 2.将客户端保存到 channelGroup 中
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        channelMap.addContext(channel.remoteAddress().toString(), ctx);
        System.err.println("有新的客户端与服务器发生连接。客户端地址：" + channel.remoteAddress());
    }

    /**
     * 当有客户端与服务器断开连接时执行此方法，此时会自动将此客户端从 channelGroup 中移除
     * 1.打印提示信息
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        System.err.println("有客户端与服务器断开连接。客户端地址：" + channel.remoteAddress() + "\n");
        channelMap.deleteContext(channel.remoteAddress().toString());
        System.out.println("通道列表：" + channelMap.getContextHashMap() + "\n");
        ctx.close();
    }

    /**
     * 读取到客户端发来的数据数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        //获取到当前channel
        Channel channel = ctx.channel();
        System.err.println("有客户端发来的数据。地址：" + channel.remoteAddress() + " 内容：" + msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        Map<String, String> msgMap = deviceTools.CwEquipment(msg.toString());
        System.out.println(msgMap);
        // 创建一个channel解析对象
        NettyChannelData nettyChannelData = new NettyChannelData();
        // 通道来源ip地址
        nettyChannelData.setIpAddress(channel.remoteAddress().toString());
    }

    /**
     * 处理异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        // 删除通道
        channelMap.deleteContext(channel.remoteAddress().toString());
        //关闭连接
        ctx.close();
        // 抛出异常
        throw new RuntimeException(cause);
    }
}
