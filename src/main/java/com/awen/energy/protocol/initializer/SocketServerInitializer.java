package com.awen.energy.protocol.initializer;

import com.awen.energy.protocol.message.SocketMsgHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SocketServerInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    private SocketMsgHandler socketMsgHandler;

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new StringDecoder());
        //处理上报数据
        pipeline.addLast(socketMsgHandler);
        pipeline.addLast(new StringEncoder());
    }
}