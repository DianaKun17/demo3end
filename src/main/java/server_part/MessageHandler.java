package server_part;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageHandler extends SimpleChannelInboundHandler<String> {

    private CommandCallback commandCallback;

    public MessageHandler(CommandCallback commandCallback) {
        this.commandCallback = commandCallback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) {
        System.out.println("Received = " + s);
        commandCallback.call(ctx, s);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("ServerMessageHandler " + cause);
        cause.printStackTrace();
    }
}