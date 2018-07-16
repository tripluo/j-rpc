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
package net.jrouter.rpc.router.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import net.jrouter.rpc.exception.SerializationException;
import net.jrouter.rpc.protocol.Protocol;
import net.jrouter.rpc.serialize.ObjectSerialization;

/**
 * Serialization for {@link Protocol} and get {@code Long} id from source {@code byte[]}.
 *
 * [0,1] -> Magic number
 * [2,9] -> Long id
 * [10] -> ObjectSerialization type
 * [11] -> Protocol type
 * [12-15] -> Data length
 *
 * @see Protocol
 */
public class ProtocolSerialization implements ObjectSerialization {

    /**
     * Protocol header size.
     */
    public static final int PROTOCAL_HEADER_SIZE = 16;

    /**
     * Magic number (22202).
     */
    public static final short MAGIC_NUMBER = 0x56ba;

    /**
     * Protocol type.
     */
    private static final byte PROTOCOL_TYPE = -1;

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
        this.supportedObjectSerializations = new HashMap<>(supportedObjectSerializations);
        //replace default ObjectSerialization
        this.supportedObjectSerializations.put(objectSerialization.getType(), objectSerialization);
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
     * @param protocol {@code Protocol}对象。
     * @param bytes {@code byte[]}对象。
     *
     * @return {@code Protocol}对象。
     */
    public <T> Protocol<T> fillProtocol(Protocol<T> protocol, byte[] bytes) {
        return fillProtocol(protocol, ByteBuffer.wrap(bytes));
    }

    /**
     * 根据原{@code ByteBuffer}填充{@code Protocol}对象信息。
     *
     * @param <T> type.
     * @param protocol {@code Protocol}对象。
     * @param buffer {@code ByteBuffer}对象。
     *
     * @return {@code Protocol}对象。
     */
    public <T> Protocol<T> fillProtocol(Protocol<T> protocol, ByteBuffer buffer) {
        if (buffer != null && protocol != null) {
            if (buffer.capacity() >= PROTOCAL_HEADER_SIZE) {
                protocol.setSerializationType(buffer.get(10));
                fillProtocol(protocol, buffer.getLong(2), buffer.get(10));
            }
        }
        return protocol;
    }

    /**
     * 填充{@code Protocol}对象信息。
     */
    private <T> Protocol<T> fillProtocol(Protocol<T> protocol, Long id, Byte serType) {
        if (id != null) {
            protocol.setId(id);
        }
        if (serType != null) {
            protocol.setSerializationType(serType);
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
        int dataLength = (data == null ? 0 : data.length);
        //java BIG_ENDIAN
        ByteBuffer buffer = ByteBuffer.allocate(PROTOCAL_HEADER_SIZE + dataLength);
        //magic number
        buffer.putShort(MAGIC_NUMBER);
        //long id
        buffer.putLong(protocol.getId() == null ? 0 : protocol.getId());
        //serialization type
        buffer.put(protocol.getSerializationType());
        //protocol type
        buffer.put(PROTOCOL_TYPE);
        //data length
        buffer.putInt(dataLength);
        if (dataLength > 0) {
            //data
            buffer.put(data);
        }
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
            //magic number
            short magicNo = buffer.getShort();
            if (magicNo != MAGIC_NUMBER) {
                throw new SerializationException("Protocol deserialize Error, unexpected magic number [" + Integer.toHexString(magicNo) + "]");
            }
            //long id
            Long id = buffer.getLong();
            //serialization type
            byte serType = buffer.get();
            ObjectSerialization os = supportedObjectSerializations.get(serType);
            if (os == null) {
                throw new SerializationException("Protocol deserialize Error, Can't find ObjectSerialization type [" + serType + "]");
            }
            //protocol type
            byte proType = buffer.get();
            //data length
            int dataLength = buffer.getInt();
            if (dataLength <= 0 || dataLength != buffer.remaining()) {
                throw new SerializationException("Protocol deserialize Error, unexpected data length [" + dataLength + "]");
            }
            //data
            byte[] data = new byte[dataLength];
            buffer.get(data);
            Protocol<?> protocol = os.deserialize(data, clz);
            //fill header data
            return (T) fillProtocol(protocol, id, serType);
        }
        throw new SerializationException(String.format("Protocol deserialize Error, require header size [%d], actual [%d]", PROTOCAL_HEADER_SIZE, buffer.capacity()));
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

    //
    public static void main(String[] args) {
        System.out.println(Integer.toHexString(22202));
        System.out.println(Integer.toHexString(022202));
    }
}
