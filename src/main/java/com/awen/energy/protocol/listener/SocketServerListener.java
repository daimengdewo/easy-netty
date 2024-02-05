package com.awen.energy.protocol.listener;

import cn.hutool.core.util.StrUtil;
import com.awen.energy.entity.EnergyImConfig;
import com.awen.energy.protocol.initializer.CwServerInitializer;
import com.awen.energy.protocol.initializer.SocketServerInitializer;
import com.awen.energy.protocol.initializer.StandardServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.net.InetSocketAddress;

@Slf4j
@Component
public class SocketServerListener {
    /**
     * 创建bootstrap
     */
    ServerBootstrap serverBootstrap = new ServerBootstrap();
    /**
     * BOSS
     */
    EventLoopGroup boss = new NioEventLoopGroup();
    /**
     * Worker
     */
    EventLoopGroup work = new NioEventLoopGroup();
    @Resource
    private EnergyImConfig imConfig;
    @Autowired
    private SocketServerInitializer socketServerInitializer;
    @Autowired
    private StandardServerInitializer standardServerInitializer;
    @Autowired
    private CwServerInitializer cwServerInitializer;

    /**
     * 开启服务线程
     */
    public void start() {
        // 从配置文件中(application.yml)获取服务端监听端口号
        serverBootstrap.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 1024);
        try {
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    int port = channel.localAddress().getPort();
                    if (port == imConfig.getPort1()) {
                        channel.pipeline().addLast(socketServerInitializer);
                    }
                    if (port == imConfig.getPort2()) {
                        channel.pipeline().addLast(standardServerInitializer);
                    }
                    if (port == imConfig.getPort3()) {
                        channel.pipeline().addLast(cwServerInitializer);
                    }
                }
            });
            //模板
            String template;
            template = "netty服务器在[{}]端口启动监听";
            // 同时绑定两个不同的端口
            ChannelFuture f1 = serverBootstrap.bind(new InetSocketAddress(imConfig.getPort1())).sync();
            System.out.println(StrUtil.format(template, imConfig.getPort1()));
            ChannelFuture f2 = serverBootstrap.bind(new InetSocketAddress(imConfig.getPort2())).sync();
            System.out.println(StrUtil.format(template, imConfig.getPort2()));
            ChannelFuture f3 = serverBootstrap.bind(new InetSocketAddress(imConfig.getPort3())).sync();
            System.out.println(StrUtil.format(template, imConfig.getPort3()));
            //关闭
            f1.channel().closeFuture().sync();
            f2.channel().closeFuture().sync();
            f3.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("[出现异常] 释放资源");
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }

    /**
     * 关闭服务器方法
     */
    @PreDestroy
    public void close() {
        log.info("关闭服务器....");
        //优雅退出
        boss.shutdownGracefully();
        work.shutdownGracefully();
    }
}