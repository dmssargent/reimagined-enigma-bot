/*
 * Copyright 2015 David Sargent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ftccommunity.services;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;

import org.ftccommunity.annonations.Inject;
import org.ftccommunity.annonations.Named;
import org.ftccommunity.annonations.RobotService;
import org.ftccommunity.gui.AuthDialog;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

@RobotService
public class DevConsole extends AbstractExecutionThreadService {
    private static final boolean SSL = System.getProperty("ssl") != null;
    private static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8992" : "8023"));

    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ServerBootstrap serverBootstrap = new ServerBootstrap();
    private Thread mainThread;
    private Context context;

    @SuppressWarnings("FieldCanBeLocal")
    @Inject
    @Named("DeviceName")
    private String wifiDeviceName = "";

    @Inject
    public DevConsole(Context ctx) {
        this.context = ctx;
    }

    /**
     * Start the service.
     */
    @Override
    protected void startUp() throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new TelnetServerInitializer(sslCtx));
        mainThread = Thread.currentThread();
    }

    /**
     * Run the service. This method is invoked on the execution thread. Implementations must respond
     * to stop requests. You could poll for lifecycle changes in a work loop:
     * <pre>
     *   public void run() {
     *     while ({@link #isRunning()}) {
     *       // perform a unit of work
     *     }
     *   }
     * </pre>
     * ...or you could respond to stop requests by implementing {@link #triggerShutdown()}, which
     * should cause {@link #run()} to return.
     */
    @Override
    protected void run() throws Exception {
        try {
            serverBootstrap.bind(PORT).sync().channel().closeFuture().sync();
        } catch (InterruptedException ignored) {
        }

        shutDown();
    }

    /**
     * Stop the service.
     */
    @Override
    protected void shutDown() throws Exception {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public void triggerShutdown() {
        mainThread.interrupt();
    }

    /**
     * Creates a newly configured {@link ChannelPipeline} for a new channel.
     */
    private class TelnetServerInitializer extends ChannelInitializer<SocketChannel> {
        private final StringDecoder DECODER = new StringDecoder();
        private final StringEncoder ENCODER = new StringEncoder();

        private final TelnetServerHandler SERVER_HANDLER = new TelnetServerHandler();

        private final SslContext sslCtx;

        public TelnetServerInitializer(SslContext sslCtx) {
            this.sslCtx = sslCtx;
        }

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            if (sslCtx != null) {
                pipeline.addLast(sslCtx.newHandler(ch.alloc()));
            }

            // Add the text line codec combination first,
            pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            // the encoder and decoder are static as these are sharable
            pipeline.addLast(DECODER);
            pipeline.addLast(ENCODER);

            // and then business logic.
            pipeline.addLast(SERVER_HANDLER);
        }
    }

    /**
     * Handles a server-side channel.
     */
    @ChannelHandler.Sharable
    public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {
        private final String endl = "\r\n";
        private AuthDialog dialog;
        private boolean verified;
        private Pattern pattern = Pattern.compile("[^\\w\\s{}\\\\/#<>;:!\"'\\.,]", Pattern.CASE_INSENSITIVE);
        private String consoleQuery;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // Send greeting for a new connection.
            FragmentManager manager = ((Activity) context).getFragmentManager();
            Fragment frag = manager.findFragmentByTag("fragment_auth");
            if (frag != null) {
                manager.beginTransaction().remove(frag).commit();
            }
            dialog = new AuthDialog();
            dialog.show(manager, "fragment_auth");

            consoleQuery = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostName()
                    + '@' + wifiDeviceName + "> ";

            ctx.write("Welcome to " + wifiDeviceName + "!\r\n");
            ctx.write("It is " + new Date() + " now.\r\n");
            ctx.write("Please enter the code shown on the screen: ");
            verified = false;
            ctx.flush();
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
            // Generate and write a response.
            String response;
            Matcher m = pattern.matcher(request);
            try {
                if (m.find()) {
                    throw new IllegalStateException();
                }
            } catch (IllegalStateException ex) {
                ctx.writeAndFlush("The command is invalid." + endl + consoleQuery);
                return;
            }

            if (!verified) {
                verified = dialog.correct(request);
                ctx.writeAndFlush(verified ? "Correct! Have a pleasant day." + endl :
                        "Nope, that is not it. Please try again. Passcode: ");
                if (!verified) {
                    return;
                }
            }

            boolean close = false;
            if (request.isEmpty()) {
                response = "Please type something." + endl;
            } else if ("bye".equals(request.toLowerCase())) {
                response = "Have a good day!" + endl;
                close = true;
            } else {
                response = "Did you say '" + request + "'?" + endl;
            }

            response += consoleQuery;

            // We do not need to write a ChannelBuffer here.
            // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
            ChannelFuture future = ctx.write(response);

            // Close the connection after sending 'Have a good day!'
            // if the client has sent 'bye'.
            if (close) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
