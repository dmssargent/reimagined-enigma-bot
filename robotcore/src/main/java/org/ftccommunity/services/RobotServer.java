package org.ftccommunity.services;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.protobuf.nano.MessageNano;

import org.ftccommunity.annonations.Inject;
import org.ftccommunity.annonations.Named;
import org.ftccommunity.annonations.RobotService;
import org.ftccommunity.messages.Robocol;
import org.ftccommunity.messaging.MessageFactory;
import org.ftccommunity.messaging.decoders.RobocolDecoder;
import org.ftccommunity.messaging.encoders.RobocolEncoder;
import org.jetbrains.annotations.NotNull;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by David on 12/20/2015.
 */
@RobotService
public class RobotServer extends AbstractExecutionThreadService {
    private static final int PORT = Integer.parseInt(System.getProperty("port", "8992"));

    private Channel robotChannel;
    private ChannelFuture robotServerFuture;

    @Inject
    @Named("ROBOT_GENERAL")
    private EventBus bus;

    @Override
    protected void startUp() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new RobocolInitializer());
        robotServerFuture = b.bind(PORT);
        robotChannel = robotServerFuture.channel();
    }

    @Override
    protected void run() throws Exception {
        robotChannel.closeFuture().sync();
//        final Robocol.Heartbeat heartbeat = new Robocol.Heartbeat();
//        MessageFactory.RobocolMessage msg = new MessageFactory.RobocolMessage() {
//            @Override
//            public MessageNano getMessage() {
//                return heartbeat;
//            }
//
//            @Override
//            public byte[] getSerialization() {
//                return MessageNano.toByteArray(heartbeat);
//            }
//
//            @Override
//            public int size() {
//                return heartbeat.getSerializedSize();
//            }
//        };
//
//        while (isRunning()) {
//            send(msg);
//            Thread.sleep(1000);
//        }
    }

    @Override
    public void triggerShutdown() {
        robotChannel.close();
    }

    @Subscribe
    public void send(MessageFactory.RobocolMessage msg) {
        robotChannel.writeAndFlush(msg);
    }

    public static class ServerUnknownEvent {
        private Robocol.RobotMessage message;

        public ServerUnknownEvent(Robocol.RobotMessage msg) {
            message = checkNotNull(msg);
        }

        @NotNull
        public Robocol.RobotMessage getMessage() {
            return message;
        }
    }

    /**
     * Creates a newly configured {@link ChannelPipeline} for a new channel.
     */
    private class RobocolInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            //pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.nulDelimiter()));
            //pipeline.addLast(new Base64Decoder());
            pipeline.addLast(new RobocolDecoder());

            //pipeline.addLast(new Base64Encoder());
            pipeline.addLast(new RobocolEncoder());

            // and then business logic.
            pipeline.addLast(new RobocolServerHandler());
        }
    }

    /**
     * Handles a server-side channel.
     */
    public class RobocolServerHandler extends SimpleChannelInboundHandler<Robocol.RobotMessage> {

        @Override
        public void channelActive(final ChannelHandlerContext ctx) {
            final Robocol.RobotMessage message = new Robocol.RobotMessage();
            final Robocol.Heartbeat heartbeat = new Robocol.Heartbeat();
            heartbeat.timeSent = System.nanoTime();
            heartbeat.inetAddress = ctx.channel().localAddress().toString();
            message.setPing(heartbeat);

            ctx.writeAndFlush(new MessageFactory.RobocolMessage<Robocol.RobotMessage>() {
                @Override
                public Robocol.RobotMessage getMessage() {
                    return message;
                }

                @Override
                public byte[] getSerialization() {
                    return MessageNano.toByteArray(message);
                }

                @Override
                public int size() {
                    return message.getSerializedSize();
                }
            });
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, Robocol.RobotMessage msg) throws Exception {
            try {
                if (msg.hasPing()) {
                    Robocol.Heartbeat heartbeat = msg.getPing();
                    long delay = System.nanoTime() - heartbeat.timeSent;
                } else if (msg.hasGamepad()) {
                    bus.post(msg.getGamepad());
                } else if (msg.hasLogReqest()) {
                    bus.post(msg.getLogReqest());
                } else if (msg.hasRobotAction()) {
                    bus.post(msg.getRobotAction());
                } else if (msg.hasTelemetry()) {
                    bus.post(msg.getTelemetry());
                } else {
                    bus.post(new ServerUnknownEvent(msg));
                }
            } catch (NullPointerException ignored) {
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
