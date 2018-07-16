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
package net.jrouter.rpc.router.client;

import net.jrouter.rpc.router.client.websocket.WebSocketClientActionFactory;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import jrouter.NotFoundException;
import jrouter.ObjectFactory;
import net.jrouter.rpc.DemoClientInterface;
import net.jrouter.rpc.RpcException;
import net.jrouter.rpc.annotation.RpcConsumer;
import net.jrouter.rpc.router.ResultCallback;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * RpcClientActionFactoryNGTest.
 *
 * @see DemoClientInterface
 */
public class RpcClientActionFactoryNGTest {

    private WebSocketClientActionFactory clientActionFactory;

    @BeforeClass
    public void setUpClass() throws Exception {
        clientActionFactory = new WebSocketClientActionFactory(null, new WebSocketClientActionFactory.Properties()) {
            @Override
            public Object invokeAction(String path, Object... params) throws RpcException {
                //do nothing just return params
                return params;
            }
        };

        clientActionFactory.addActions(TestInterface.class);
        clientActionFactory.addActions(DemoClientInterface.class);
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        if (clientActionFactory != null) {
            clientActionFactory.clear();
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        assertNotNull(clientActionFactory);
    }

    /**
     * Test of getClient method, of class WebSocketClientActionFactory.
     */
    @Test
    public void testGetClient() {
        assertNull(clientActionFactory.getClient(ResultCallback.class));
        assertNull(clientActionFactory.getClient(NotFoundException.class));

        TestInterface testInterface = clientActionFactory.getClient(TestInterface.class);
        assertNotNull(testInterface);
        assertEquals(testInterface.size(), 0);
        assertEquals(testInterface.hashCode(), 0);
        assertEquals(testInterface.isEmpty(), false);
        assertEquals(testInterface.toArray(), null);

        DemoClientInterface client = clientActionFactory.getClient(DemoClientInterface.class);
        assertNotNull(client);
        assertEquals(client.time(), 0L);
        assertEquals(client.echo("test"), null);
        assertEquals(client.checkProtocol(Collections.EMPTY_LIST), false);
    }

    /**
     * Test of getClient method, of class WebSocketClientActionFactory.
     */
    @Test
    public void testGetClientClass() {
        ObjectFactory javassistObjectFactory = clientActionFactory.getClientObjectFactory();

        TestInterface testInterface = clientActionFactory.getClient(TestInterface.class);
        assertNotEquals(testInterface.getClass(), TestInterface.class);
        assertEquals(javassistObjectFactory.getClass(testInterface), TestInterface.class);

        DemoClientInterface client = clientActionFactory.getClient(DemoClientInterface.class);
        assertNotEquals(client.getClass(), DemoClientInterface.class);
        assertEquals(javassistObjectFactory.getClass(client), DemoClientInterface.class);
    }

    /**
     * 测试接口。
     */
    @RpcConsumer(proxyInterfaces = {List.class, Map.class, NoSuchFieldException.class})
    public static interface TestInterface extends List<String>, Comparator<Number> {

        @Override
        int size();
    }
}
