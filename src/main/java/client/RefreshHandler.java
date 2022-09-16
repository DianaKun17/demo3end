package client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RefreshHandler extends SimpleChannelInboundHandler<String[]> {

    private RefreshCallback rc;

    public RefreshHandler(RefreshCallback rc) {
        this.rc = rc;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String[] list) {
        rc.call(list);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("ClientRefreshHandler " + cause);
    }
}
