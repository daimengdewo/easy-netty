package com.awen.energy.protocol.message;

import cn.hutool.core.util.StrUtil;
import com.awen.energy.config.common.Code;
import com.awen.energy.exception.BusinessException;
import com.awen.energy.protocol.StatusType;
import com.awen.energy.service.ChannelGroupService;
import com.awen.energy.tool.DeviceTools;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@ChannelHandler.Sharable
public class SocketMsgHandler extends SimpleChannelInboundHandler<String> {

    @Autowired
    private DeviceTools deviceTools;
    @Autowired
    private ChannelGroupService groupService;
    @Autowired
    private ChannelMap channelMap;

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
        Map<String, String> msgMap = deviceTools.analysisEquipment(msg.toString());

        //数据完整性校验
        if (StrUtil.isEmptyIfStr(msgMap.get("cmd"))) {
            channelMap.deleteContext(channel.remoteAddress().toString());
            ctx.close();
            throw new BusinessException(Code.COMMON_ERR, "数据完整性校验失败！");
        }

        //通道地址缓存到redis
        deviceTools.saveChannelData(channel.remoteAddress().toString(), "adr_" + msgMap.get("dev"), 70);

        //判断上报的数据
        StatusType type = StatusType.getByType(msgMap.get("cmd"), StatusType.class);
        switch (Objects.requireNonNull(type)) {
            //心跳包
            case HEARTBEAT:
                System.out.println("心跳包：" + msgMap);
                System.out.println("接收时间：" + LocalDateTime.now());
                System.out.println("--------------------------------------------\n");
                String template = "msg:dev:{},mark:1,msg:{},cmd:hbt,sta:00";
                //返回
                String order = StrUtil.format(template, msgMap.get("dev"), msgMap.get("msg"));
                //先保存到redis
                deviceTools.saveChannelData(msgMap.get("sig"), msgMap.get("dev"), 70);
                //发送
                groupService.channelGroupUtil(msgMap.get("dev"), order);
                break;
            //登录操作
            case LOGIN:
                System.out.println("登录接收：" + msgMap);
                String template2 = "msg:dev:{},mark:1,msg:{},cmd:log,sta:00";
                System.out.println("--------------------------------------------\n");
                //保存推送地址
                String addr = deviceTools.saveChannelPushAddr(msgMap.get("dev")).get();
                if (StrUtil.isNotEmpty(addr)) {
                    //登陆返回
                    String order2 = StrUtil.format(template2, msgMap.get("dev"), msgMap.get("msg"));
                    groupService.channelGroupUtil(msgMap.get("dev"), order2);
                    //推送
                    Map<String, String> res = new HashMap<>();
                    res.put("mode", "tcp");
                    res.put("dev", msgMap.get("dev"));
                    res.put("mark", msgMap.get("mark"));
                    res.put("msg", msgMap.get("msg"));
                    res.put("sta", msgMap.get("sta"));
                    res.put("cmd", msgMap.get("cmd"));
                    res.put("time", String.valueOf(System.currentTimeMillis()));
                    String request = deviceTools.sendPostRequest(addr, res);
                    System.out.printf("推送返回：" + request + "\n");
                }
                break;
            //指令执行结果回调
            case OPEN:
                System.out.println("业务逻辑：" + msgMap);
                System.out.println("--------------------------------------------\n");
                //获取推送地址
                String pushAddr = deviceTools.getChannelPushAddr(msgMap.get("dev"));
                //定义哈希表
                Map<String, String> res = new HashMap<>();
                res.put("mode", "tcp");
                res.put("dev", msgMap.get("dev"));
                res.put("mark", msgMap.get("mark"));
                res.put("msg", msgMap.get("msg"));
                res.put("cmd", msgMap.get("cmd"));
                res.put("sta", msgMap.get("sta"));
                res.put("ctl", msgMap.get("ctl"));
                res.put("door", msgMap.get("door"));
                res.put("time", String.valueOf(System.currentTimeMillis()));
                System.out.printf("推送地址：" + pushAddr + "\n");
                //推送
                String request = deviceTools.sendPostRequest(pushAddr, res);
                System.out.printf("推送返回：" + request + "\n");
                break;
        }
    }

    /**
     * 处理异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        log.error("发生异常。异常信息：{}", cause.toString());
        // 删除通道
        channelMap.deleteContext(channel.remoteAddress().toString());
        ctx.close();
    }
}
