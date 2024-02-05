package com.awen.energy.protocol.initializer;

import com.awen.energy.protocol.message.SocketMsgHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StandardServerInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    private SocketMsgHandler socketMsgHandler;

    private static final int MAX_FRAME_LENGTH = 512; // 最大消息长度

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 使用 DelimiterBasedFrameDecoder 进行拆包处理，指定 \r\n 作为分隔符
        pipeline.addLast(new DelimiterBasedFrameDecoder(MAX_FRAME_LENGTH, Delimiters.lineDelimiter()));
        //处理上报数据
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(socketMsgHandler);
        pipeline.addLast(new StringEncoder());
    }
}