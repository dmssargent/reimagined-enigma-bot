package org.ftccommunity.reimagined;

import org.ftccommunity.messages.Robocol;
import org.ftccommunity.reimagined.messaging.MessageFactory;
import org.ftccommunity.reimagined.messaging.decoders.RobocolDecoder;
import org.ftccommunity.reimagined.messaging.encoders.RobocolEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Simplistic telnet client.
 */
public final class RobotClient {
    static final int PORT = 8992;

    public static void main(String[] args) throws Exception {
        System.out.print("Enter IP: ");
        Scanner input = new Scanner(System.in);
        final String HOST = input.nextLine();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new TelnetClientInitializer());

            // Start the connection attempt.
            Channel ch = b.connect(HOST, PORT).sync().channel();

            // Read commands from the stdin.
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (; ; ) {
                String line = in.readLine();
                if (line == null) {
                    continue;
                }

                // Sends the received line to the server.
                Robocol.Heartbeat build = Robocol.Heartbeat.newBuilder()
                        .setInetAddress(ch.localAddress().toString())
                        .setTimeSent(System.nanoTime()).build();
                Robocol.RobotMessage message = Robocol.RobotMessage.newBuilder().setPing(build).build();
                ch.writeAndFlush(new MessageFactory.RobocolMessage<Robocol.RobotMessage>() {
                    @Override
                    public Robocol.RobotMessage getMessage() {
                        return message;
                    }

                    @Override
                    public byte[] getSerialization() {
                        return message.toByteArray();
                    }

                    @Override
                    public int size() {
                        return message.getSerializedSize();
                    }
                });

                // If user typed the 'bye' command, wait until the server closes
                // the connection.
                if ("bye".equals(line.toLowerCase())) {
                    break;
                }
            }

            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * Creates a newly configured {@link ChannelPipeline} for a new channel.
     */
    public static class TelnetClientInitializer extends ChannelInitializer<SocketChannel> {

        private final RobocolDecoder DECODER = new RobocolDecoder();
        private final RobocolEncoder ENCODER = new RobocolEncoder();

        private final TelnetClientHandler CLIENT_HANDLER = new TelnetClientHandler();

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();

            // Add the text line codec combination first,
            //pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.nulDelimiter()));
            pipeline.addLast(DECODER);

            pipeline.addLast(ENCODER);

            // and then business logic.
            pipeline.addLast(CLIENT_HANDLER);
        }
    }

    /**
     * Handles a client-side channel.
     */
    @ChannelHandler.Sharable
    public static class TelnetClientHandler extends SimpleChannelInboundHandler<Robocol.RobotMessage> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Robocol.RobotMessage msg) throws Exception {
            System.err.println(msg.toString());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
