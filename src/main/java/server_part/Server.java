package server_part;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import java.util.Date;

public class Server {

    private final int PORT = 45003;
    private ChannelFuture channelFuture;

    public Server(SaveFileCallback saveFileCallback,
                  CommandCallback commandCallback,
                  DeleteFileCallback deleteFileCallback) {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new FileHandler(saveFileCallback),
                                    new MessageHandler(commandCallback),
                                    new DeleteFileHandler(deleteFileCallback)
                            );
                        }
                    });
            channelFuture = serverBootstrap.bind(PORT).sync();
            System.out.println("Server started " + new Date());
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
           e.printStackTrace();
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
