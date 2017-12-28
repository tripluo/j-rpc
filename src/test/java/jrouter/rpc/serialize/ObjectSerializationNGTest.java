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
package jrouter.rpc.serialize;

import com.esotericsoftware.kryo.KryoException;
import java.util.Arrays;
import jrouter.rpc.RpcException;
import jrouter.rpc.protocol.RpcProtocol;
import jrouter.rpc.serialize.fastjson.FastJsonSerialization;
import jrouter.rpc.serialize.fst.FSTSerialization;
import jrouter.rpc.serialize.hessian.HessianSerialization;
import jrouter.rpc.serialize.jackson.JacksonSerialization;
import jrouter.rpc.serialize.kryo.KryoSerialization;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * ObjectSerializationNGTest.
 */
public class ObjectSerializationNGTest {

    private final RpcProtocol protocol = new RpcProtocol();

    private final RpcException exception = new RpcException("SerializationException",
            new IllegalArgumentException("IllegalArgument Exception"));

    @BeforeMethod
    public void beforeMethod() {
        protocol.setId(Long.MIN_VALUE);
        protocol.setPath("/test");
        protocol.setParameters(new Object[]{1, "1", Arrays.asList(Long.MAX_VALUE, "zzz")});
    }

    /**
     * Test of serialize method, of class ObjectSerialization.
     */
    @Test
    public void testSerialize() {
        ObjectSerialization[] objectSerializations = {
            new FSTSerialization(),
            new FastJsonSerialization(),
            new JavaSerialization(),
            new HessianSerialization(),
            new JacksonSerialization(),
            new KryoSerialization()
        };

        for (ObjectSerialization serialization : objectSerializations) {
            byte[] result = null;
            result = serialization.serialize(protocol);
            assertNotNull(result);

            RpcProtocol back = serialization.deserialize(result, RpcProtocol.class);
            assertNotNull(back);
            assertNotNull(back.getId());

            assertEquals(back.getId(), protocol.getId());
            assertEquals(back.getPath(), protocol.getPath());
            assertEquals(back.getParameters(), protocol.getParameters());

        }
    }

    /**
     * Test of serialize method, of class KryoSerialization.
     */
    @Test(expectedExceptions = KryoException.class)
    public void testKryoSerialization() {
        KryoSerialization serialization = new KryoSerialization();
        byte[] result = serialization.serialize(exception);
        //RpcException deserialize exception
        RpcException ex = serialization.deserialize(result, RpcException.class);
        assertEquals(ex.getLocalizedMessage(), exception.getLocalizedMessage());
        assertEquals(ex.getCause().getLocalizedMessage(), exception.getCause().getLocalizedMessage());
    }

    /**
     * Test of serialize method, of class JacksonSerialization.
     */
    @Test(expectedExceptions = RpcException.class)
    public void testJacksonSerialization() {
        JacksonSerialization serialization = new JacksonSerialization();

        //circular reference
        protocol.setParameters(new Object[]{protocol});
        byte[] result = serialization.serialize(protocol);
        RpcProtocol back = serialization.deserialize(result, RpcProtocol.class);
        assertNotNull(back);

        result = serialization.serialize(exception);
        RpcException ex = serialization.deserialize(result, RpcException.class);
        assertNotNull(ex);
    }

    /**
     * Test of serialize method, of class FSTSerialization.
     */
    @Test
    public void testFSTSerialization() {
        FSTSerialization serialization = new FSTSerialization();
        //circular reference
        protocol.setParameters(new Object[]{protocol});
        byte[] result = serialization.serialize(protocol);
        RpcProtocol back = serialization.deserialize(result, RpcProtocol.class);
        assertNotNull(back);
        assertEquals(back.getParameters()[0], back);

        //TODO check back
        result = serialization.serialize(exception);
        RpcException ex = serialization.deserialize(result, RpcException.class);
        assertEquals(ex.getLocalizedMessage(), exception.getLocalizedMessage());
        assertEquals(ex.getCause().getLocalizedMessage(), exception.getCause().getLocalizedMessage());
    }

    /**
     * Test of serialize method, of class FastJsonSerialization.
     */
    @Test
    public void testFastJsonSerialization() {
        FastJsonSerialization serialization = new FastJsonSerialization();
        //circular reference
        protocol.setParameters(new Object[]{protocol});
        byte[] result = serialization.serialize(protocol);
        RpcProtocol back = serialization.deserialize(result, RpcProtocol.class);
        assertNotNull(back);
        //TODO check back ERROR!
        //assertEquals(back.getParameters()[0], back);

        result = serialization.serialize(exception);
        RpcException ex = serialization.deserialize(result, RpcException.class);
        assertEquals(ex.getLocalizedMessage(), exception.getLocalizedMessage());
        assertEquals(ex.getCause().getLocalizedMessage(), exception.getCause().getLocalizedMessage());
    }

}
