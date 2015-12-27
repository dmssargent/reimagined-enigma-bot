package org.ftccommunity.messaging;

import com.google.protobuf.nano.MessageNano;

/**
 * Created by David on 12/21/2015.
 */
public class MessageFactory {

    public interface RobocolMessage<T extends MessageNano> {
        T getMessage();

        byte[] getSerialization();

        int size();
    }
}
