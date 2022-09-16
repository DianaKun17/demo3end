package client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import common.FileMessage;

public class FileHandler extends SimpleChannelInboundHandler<FileMessage> {

    private FileHandlerCallback fhc;

    public FileHandler(FileHandlerCallback fhc) {
        this.fhc = fhc;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileMessage fm) throws Exception {
        fhc.call(fm);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("ClientFileHandler " + cause);
    }
}
