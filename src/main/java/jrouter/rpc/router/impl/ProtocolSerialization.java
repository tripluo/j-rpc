/*
 * Copyright (C) 2010-2111 sunjumper@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package jrouter.rpc.router.impl;

import java.nio.ByteBuffer;
import java.util.Map;
import jrouter.rpc.exception.SerializationException;
import jrouter.rpc.protocol.Protocol;
import jrouter.rpc.serialize.ObjectSerialization;

/**
 * Serialization for {@link Protocol} and get {@code Long} id from source {@code byte[]}.
 *
 * [0,7] -> Long id
 * [8] -> ObjectSerialization type
 *
 * @see Protocol
 */
public class ProtocolSerialization implements ObjectSerialization {

    //protocal header size
    private static final int PROTOCAL_HEADER_SIZE = 9;

    /**
     * Delegated default ObjectSerialization.
     */
    private final ObjectSerialization objectSerialization;

    /**
     * Supported Object Serializations.
     */
    private final Map<Byte, ObjectSerialization> supportedObjectSerializations;

    /**
     * Constructor.
     *
     * @param objectSerialization Object Serialization.
     * @param supportedObjectSerializations Supported Object Serializations.
     */
    public ProtocolSerialization(ObjectSerialization objectSerialization,
            Map<Byte, ObjectSerialization> supportedObjectSerializations) {
        this.objectSerialization = objectSerialization;
        this.supportedObjectSerializations = supportedObjectSerializations;
    }

    /**
     * 根据原{@code ByteBuffer}获取{@code ObjectSerialization}。
     *
     * @param buffer {@code ByteBuffer}对象。
     *
     * @return ObjectSerialization.
     */
    public ObjectSerialization getObjectSerialization(ByteBuffer buffer) {
        return (buffer != null && buffer.capacity() >= 9) ? supportedObjectSerializations.get(buffer.get(8)) : null;
    }

    /**
     * 根据{@code byte[]}填充{@code Protocol}对象信息。
     *
     * @param <T> type.
     * @param bytes {@code byte[]}对象。
     * @param protocol {@code Protocol}对象。
     *
     * @return {@code Protocol}对象。
     */
    public <T> Protocol<T> fillProtocol(byte[] bytes, Protocol<T> protocol) {
        return fillProtocol(ByteBuffer.wrap(bytes), protocol);
    }

    /**
     * 根据原{@code ByteBuffer}填充{@code Protocol}对象信息。
     *
     * @param <T> type.
     * @param buffer {@code ByteBuffer}对象。
     * @param protocol {@code Protocol}对象。
     *
     * @return {@code Protocol}对象。
     */
    public <T> Protocol<T> fillProtocol(ByteBuffer buffer, Protocol<T> protocol) {
        if (buffer != null && protocol != null) {
            if (buffer.capacity() >= 8) {
                protocol.setId(buffer.getLong(0));
            }
            if (buffer.capacity() >= 9) {
                protocol.setSerializationType(buffer.get(8));
            }
        }
        return protocol;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Protocol<?>) {
            ByteBuffer buffer = serializeByteBuffer((Protocol) obj);
            return buffer.array();
        }
        return objectSerialization.serialize(obj);
    }

    /**
     * Serialize {@code Protocol} object to {@code ByteBuffer} object.
     *
     * @param protocol {@code Protocol} object.
     *
     * @return {@code ByteBuffer}.
     */
    public ByteBuffer serializeByteBuffer(Protocol<?> protocol) {
        ObjectSerialization os = supportedObjectSerializations.get(protocol.getSerializationType());
        if (os == null) {
            throw new SerializationException("Protocol serialize Error, Can't find ObjectSerialization type [" + protocol.getSerializationType() + "]");
        }
        byte[] data = os.serialize(protocol);
        ByteBuffer buffer = (data == null ? ByteBuffer.allocate(PROTOCAL_HEADER_SIZE) : ByteBuffer.allocate(PROTOCAL_HEADER_SIZE + data.length));
        //long id
        buffer.putLong(protocol.getId());
        //serialization type
        buffer.put(protocol.getSerializationType());
        buffer.put(data);
        //position 0 ready to read
        buffer.rewind();
        return buffer;
    }

    /**
     * Deserialize {@code ByteBuffer} object to {@code Protocol} object.
     *
     * @param <T> Protocol type.
     * @param buffer {@code ByteBuffer} object.
     * @param clz {@code Protocol} class type.
     *
     * @return {@code Protocol} object.
     */
    public <T extends Protocol<?>> T deserializeByteBuffer(ByteBuffer buffer, Class<T> clz) {
        if (buffer == null) {
            return null;
        }
        if (buffer.capacity() >= PROTOCAL_HEADER_SIZE) {
            buffer.rewind();
            //long id
            Long id = buffer.getLong();
            //serialization type
            byte type = buffer.get();
            ObjectSerialization os = supportedObjectSerializations.get(type);
            if (os == null) {
                throw new SerializationException("Protocol deserialize Error, Can't find ObjectSerialization type [" + type + "]");
            }
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            return os.deserialize(data, clz);
        }
        throw new SerializationException("Protocol deserialize Error, require header size [" + PROTOCAL_HEADER_SIZE + "], actual [" + buffer.capacity() + "].");
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) {
        if (Protocol.class.isAssignableFrom(clz)) {
            return (T) deserializeByteBuffer(ByteBuffer.wrap(bytes), (Class<Protocol>) clz);
        }
        return objectSerialization.deserialize(bytes, clz);
    }

    @Override
    public byte getType() {
        return Byte.MIN_VALUE;
    }

}
