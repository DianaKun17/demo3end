package server_part;

import common.DeleteFileMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DeleteFileHandler extends SimpleChannelInboundHandler<DeleteFileMessage> {

    private DeleteFileCallback deleteFileCallback;

    public DeleteFileHandler(DeleteFileCallback deleteFileCallback) {
        this.deleteFileCallback = deleteFileCallback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DeleteFileMessage dfm) {
        System.out.println(dfm.getName() + " - to delete");
        deleteFileCallback.call(dfm);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("DeleteFileHandler " + cause);
    }
}
