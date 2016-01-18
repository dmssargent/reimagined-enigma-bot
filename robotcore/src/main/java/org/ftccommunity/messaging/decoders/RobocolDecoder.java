package org.ftccommunity.messaging.decoders;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;

import android.util.Log;

import org.ftccommunity.messages.nano.Robocol;

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
        } catch (InvalidProtocolBufferNanoException ex) {
            Log.e(RobocolDecoder.class.getSimpleName(), "Failed to decode protobuf");
        }

    }
}
