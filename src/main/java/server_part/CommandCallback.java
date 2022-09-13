package server_part;

import io.netty.channel.ChannelHandlerContext;

public interface CommandCallback {

    void call(ChannelHandlerContext ctx, String s);
}
