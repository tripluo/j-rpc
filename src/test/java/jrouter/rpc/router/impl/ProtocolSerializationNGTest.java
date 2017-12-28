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
import jrouter.rpc.protocol.Protocol;
import jrouter.rpc.protocol.RpcProtocol;
import jrouter.rpc.serialize.ObjectSerialization;
import jrouter.rpc.serialize.hessian.HessianSerialization;
import jrouter.rpc.util.Constants;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ProtocolSerializationNGTest.
 */
public class ProtocolSerializationNGTest {

    private ProtocolSerialization protocolSerialization;

    @BeforeClass
    public void setUpClass() throws Exception {
        protocolSerialization = new ProtocolSerialization(new HessianSerialization(), Constants.SUPPORTED_OBJECT_SERIALIZATIONS);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        assertNotNull(protocolSerialization);
    }

    /**
     * Test of deserialize method, of class ProtocolSerialization.
     */
    @Test
    public void testDeserialize() {
        Long time = System.currentTimeMillis();
        RpcProtocol protocol = new RpcProtocol();
        protocol.setId(time);
        protocol.setPath("/test");
        protocol.setParameters(new Object[]{1, "1"});
        ByteBuffer result = protocolSerialization.serializeByteBuffer(protocol);
        assertNotNull(result);

        //getId can run many times
//        assertEquals(protocolSerialization.getId(result), time);
//        assertEquals(protocolSerialization.getId(result), time);
        RpcProtocol back = protocolSerialization.deserializeByteBuffer(result, RpcProtocol.class);

        assertNotNull(back);
        assertNotNull(back.getId());

        assertEquals(back.getId(), protocol.getId());
        assertEquals(back.getPath(), protocol.getPath());
        assertEquals(back.getParameters(), protocol.getParameters());

    }

    /**
     * Test of fillProtocol method, of class ProtocolSerialization.
     */
    @Test
    public void testFillProtocol_ByteBuffer_Protocol() {
        Long time = System.currentTimeMillis();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putLong(time);
        buffer.put(protocolSerialization.getType());
        Protocol result = protocolSerialization.fillProtocol(buffer, new RpcProtocol());
        assertEquals(time, result.getId());
        assertEquals(protocolSerialization.getType(), result.getSerializationType());
    }

}
