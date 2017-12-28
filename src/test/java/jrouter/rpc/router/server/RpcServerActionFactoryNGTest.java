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
package jrouter.rpc.router.server;

import java.util.Arrays;
import java.util.Collections;
import jrouter.rpc.DemoService;
import jrouter.rpc.RpcException;
import jrouter.rpc.protocol.RpcProtocol;
import lombok.extern.slf4j.Slf4j;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * RpcServerActionFactoryNGTest.
 *
 * @see DemoService
 */
@Slf4j
public class RpcServerActionFactoryNGTest {

    private RpcServerActionFactory<?> serverActionFactory;

    @BeforeClass
    public void setUpClass() throws Exception {
        serverActionFactory = new RpcServerActionFactory(Collections.EMPTY_MAP);
        serverActionFactory.addActions(DemoService.class);
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        if (serverActionFactory != null) {
            serverActionFactory.clear();
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        assertNotNull(serverActionFactory);
    }

    /**
     * Test of onMessage method, of class RpcServerActionFactory.
     *
     * @see DemoService
     */
    @Test
    public void testOnMessage() {
        RpcProtocol protocol = new RpcProtocol();
        protocol.setId(1L);
        protocol.setPath(parsePath(DemoService.class, "time"));

        Object res = null;
        res = serverActionFactory.onMessage(serverActionFactory.getObjectSerialization().serialize(protocol), null);
        assertNotNull(res);
        assertEquals(res.getClass(), Long.class);

        protocol.setPath(parsePath(DemoService.class, "echo"));
        protocol.setParameters(new Object[]{"tester"});
        res = serverActionFactory.onMessage(serverActionFactory.getObjectSerialization().serialize(protocol), null);
        assertEquals(res, "tester");

        protocol.setPath(parsePath(DemoService.class, "getProtocol"));
        protocol.setParameters(new Object[]{1000, "abcde", Arrays.asList("a", "bb", "ccc")});
        RpcProtocol resProtocol = serverActionFactory.onMessage(serverActionFactory.getObjectSerialization().serialize(protocol), null);
        assertEquals(protocol.getId(), resProtocol.getId());
        assertEquals(protocol.getPath(), resProtocol.getPath());

    }

    /**
     * Test of onMessage method, of class RpcServerActionFactory.
     */
    @Test(expectedExceptions = RpcException.class)
    public void testOnMessage_not_found() {
        RpcProtocol protocol = new RpcProtocol();
        protocol.setId(1L);
        protocol.setPath(parsePath(DemoService.class, "not_found"));
        serverActionFactory.onMessage(serverActionFactory.getObjectSerialization().serialize(protocol), null);
    }

    /**
     * Test of onMessage method, of class RpcServerActionFactory.
     *
     * @see DemoService#exception
     */
    @Test(expectedExceptions = RpcException.class)
    public void testOnMessage_excpetion() {
        RpcProtocol protocol = new RpcProtocol();
        protocol.setId(1L);

        protocol.setPath(parsePath(DemoService.class, "exception"));
        protocol.setParameters(new Object[]{"random"});
        serverActionFactory.onMessage(serverActionFactory.getObjectSerialization().serialize(protocol), null);
    }

    /**
     * Test of handleError method, of class RpcServerActionFactory.
     */
    @Test(expectedExceptions = RpcException.class)
    public void testHandleError() {
        serverActionFactory.onMessage(serverActionFactory.getObjectSerialization().serialize("not a protocol"), null);
    }

    /**
     * Parse path.
     *
     * @see RpcServerActionFactory#getNamespace
     */
    private String parsePath(Class<?> cls, String methodName) {
        return "/" + cls.getCanonicalName() + "/" + methodName;
    }

}
