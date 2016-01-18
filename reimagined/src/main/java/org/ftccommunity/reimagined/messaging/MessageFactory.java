package org.ftccommunity.reimagined.messaging;

import com.google.protobuf.Message;

/**
 * Created by David on 12/21/2015.
 */
public class MessageFactory {

    public interface RobocolMessage<T extends Message> {
        T getMessage();

        byte[] getSerialization();

        int size();
    }
}
