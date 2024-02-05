package com.awen.energy.protocol.initializer;

import com.awen.energy.protocol.handler.CWHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CwServerInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    private CWHandler cwHandler;

    private static final int MAX_FRAME_LENGTH = 512; // 最大消息长度

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 使用 DelimiterBasedFrameDecoder 进行拆包处理，指定 &E=0 作为分隔符
        ByteBuf delimiter = Unpooled.copiedBuffer("&E=0".getBytes());
        pipeline.addLast(new DelimiterBasedFrameDecoder(MAX_FRAME_LENGTH, delimiter));
        //处理上报数据
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(cwHandler);
        pipeline.addLast(new StringEncoder());
    }
}