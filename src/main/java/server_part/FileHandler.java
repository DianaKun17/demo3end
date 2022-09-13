package server_part;

import common.FileMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class FileHandler extends SimpleChannelInboundHandler<FileMessage> {

    private SaveFileCallback saveFileCallback;

    public FileHandler(SaveFileCallback saveFileCallback) {
        this.saveFileCallback = saveFileCallback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileMessage fileMessage)  {
        System.out.println(fileMessage.getName() + " size: " + fileMessage.getSize());
        saveFileCallback.call(fileMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}
