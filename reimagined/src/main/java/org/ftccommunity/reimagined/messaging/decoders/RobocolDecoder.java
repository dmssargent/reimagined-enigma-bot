package org.ftccommunity.reimagined.messaging.decoders;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Created by David on 12/21/2015.
 */
public class RobocolDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int size = msg.readInt();
        byte[] data = msg.readBytes(size).array();
        try {
            out.add(Robocol.RobotMessage.parseFrom(data));
        } catch (InvalidProtocolBufferException ex) {
            System.err.println("Failed to decode protobuf");
        }

    }
}
