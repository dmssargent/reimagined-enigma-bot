package org.ftccommunity.reimagined.messaging.encoders;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.ftccommunity.reimagined.messaging.MessageFactory;

/**
 * Created by David on 12/21/2015.
 */
public class RobocolEncoder extends MessageToMessageEncoder<MessageFactory.RobocolMessage<?>> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageFactory.RobocolMessage<?> msg, List<Object> out) throws Exception {
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeInt(msg.size());
        buffer.writeBytes(msg.getSerialization());
        out.add(buffer);
    }
}
