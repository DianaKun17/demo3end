package client;

import common.DeleteFileMessage;
import common.FileMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class Network {

    private SocketChannel channel;
    private int PORT = 45003;
    private String HOST = "localhost";


    private ChannelFuture future;

    public Network(FileHandlerCallback fhc,
                   RefreshCallback rc) {
        Thread thread = new Thread(() -> {
            EventLoopGroup worker = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(worker)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel c) {
                                channel = c;
                                c.pipeline().addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new FileHandler(fhc),
                                        new RefreshHandler(rc)
                                );
                            }
                        });

                future = bootstrap.connect(HOST, PORT).sync();
                loginAttempt();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                worker.shutdownGracefully();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void send(FileMessage f) {
        channel.writeAndFlush(f);
    }

    public void delete(DeleteFileMessage d) {
        channel.writeAndFlush(d);
    }

    public void sendMsg(String msg) {
        channel.writeAndFlush(msg);
    }

    public void loginAttempt() {
        String propsPath = System.getProperty("user.home") + "/cloud-storage/prop.properties";
        File props = new File(propsPath);
        Properties p = new Properties();
        if (props.exists()) {
            try {
                p.load(new FileInputStream(propsPath));
                if (!p.getProperty("login").equals("") && !p.getProperty("password").equals("")) {
                    sendMsg("/login " + p.getProperty("login") + " " + p.getProperty("password"));
                }
            } catch (IOException | NullPointerException e) {
                System.out.println("auto login cancelled, missing properties");
            }
        }
    }


}
