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
package jrouter.rpc.router.client.websocket;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import jrouter.rpc.DemoClientInterface;
import jrouter.rpc.RpcException;
import jrouter.rpc.protocol.Protocol;
import jrouter.rpc.router.ResultCallback;
import jrouter.rpc.router.client.RpcClientContext;
import jrouter.rpc.router.client.websocket.util.SessionPooledObjectFactory;
import jrouter.rpc.router.server.websocket.DemoServerEndpoint;
import jrouter.rpc.router.server.websocket.WebSocketServerActionFactoryNGTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * WebSocketClientActionFactoryNGTest.
 */
@Slf4j
public final class WebSocketClientActionFactoryNGTest extends WebSocketServerActionFactoryNGTest {

    private WebSocketClientActionFactory webSocketClientActionFactory;

    //3s
    private static final int DEFAULT_WAIT_SECONDS = 3;

    @BeforeClass
    public final void setUpWebSocketClient() throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        DemoClientEndpoint clientEndpoint = new DemoClientEndpoint();
        String demoWebSocketURL = "ws://localhost:" + getPort() + CONTEXT_PATH + DemoServerEndpoint.SERVER_URL;
        log.info("Connecting to Server : {}", demoWebSocketURL);
//        final Session session = container.connectToServer(clientEndpoint, new URI(demoWebSocketURL));

        ObjectPool<Session> pool = new GenericObjectPool<>(
                new SessionPooledObjectFactory(container, clientEndpoint, new URI(demoWebSocketURL)));

        webSocketClientActionFactory = new WebSocketClientActionFactory(pool, Collections.EMPTY_MAP);
        clientEndpoint.setRpcActionFactory(webSocketClientActionFactory);
        webSocketClientActionFactory.addActions(DemoClientInterface.class);
    }

    @AfterClass
    public final void tearDownWebSocketClient() throws Exception {
        if (webSocketClientActionFactory != null) {
            webSocketClientActionFactory.clear();
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        assertNotNull(webSocketClientActionFactory);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getClient method, of class WebSocketClientActionFactory.
     */
    @Test
    public void testGetClient() {
        DemoClientInterface client = webSocketClientActionFactory.getClient(DemoClientInterface.class);
        final int waitSeconds = DEFAULT_WAIT_SECONDS;
        assertNotNull(client);
        ResultCallback callback = null;
        callback = new ResultCallback<Long>() {
            @Override
            public Long callback(Future<Long> result) {
                Long time = null;
                try {
                    time = result.get(waitSeconds, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException | ClassCastException ex) {
                    fail("Not happen", ex);
                }
                log.info("Received time : {}", time);
                assertTrue(time instanceof Long);
                return time;
            }
        };
        RpcClientContext.get().setResultCallback(callback);
        client.time();

        final String msg1 = "您好, hello!";
        callback = new ResultCallback<String>() {
            @Override
            public String callback(Future<String> result) {
                String echo = null;
                try {
                    echo = result.get(waitSeconds, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    fail("Not happen", ex);
                }
                assertEquals(echo, msg1);
                return echo;
            }
        };
        RpcClientContext.get().setResultCallback(callback);
        client.echo(msg1);

        final String msg2 = "hello，您好！";
        client.echo(msg2, new ResultCallback<String>() {
            @Override
            public String callback(Future<String> result) {
                String echo = null;
                try {
                    echo = result.get(waitSeconds, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    fail("Not happen", ex);
                }
                assertEquals(echo, msg2);
                return echo;
            }
        });

    }

    /**
     * Test of getClient method, of class WebSocketClientActionFactory.
     */
    @Test
    public void testGetClient_complex() {
        DemoClientInterface client = webSocketClientActionFactory.getClient(DemoClientInterface.class);
        final int waitSeconds = DEFAULT_WAIT_SECONDS;
        final String msg = "测试复杂对象!";
        client.getProtocol(99, msg, Collections.EMPTY_LIST, new ResultCallback<Protocol<String>>() {
            @Override
            public Protocol<String> callback(Future<Protocol<String>> result) {
                Protocol<String> protocol = null;
                try {
                    protocol = result.get(waitSeconds, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    fail("Not happen", ex);
                }
                log.info("Received Protocol : {}", protocol);
                assertSame(protocol.getResult(), protocol);
                return protocol;
            }
        });
    }

    /**
     * Test of getClient method, of class WebSocketClientActionFactory.
     */
    @Test
    public void testGetClient_not_found() {
        DemoClientInterface client = webSocketClientActionFactory.getClient(DemoClientInterface.class);
        ResultCallback callback = new ResultCallback<RpcException>() {
            @Override
            public RpcException callback(Future<RpcException> result) {
                RpcException resEx = null;
                try {
                    resEx = result.get(1, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    fail("Not happen", ex);
                }
                assertNotNull(resEx);
                assertEquals(resEx.getCause().getClass(), jrouter.NotFoundException.class);
                log.info("Received no such method : {}", resEx.getLocalizedMessage());
                return resEx;
            }
        };
        RpcClientContext.get().setResultCallback(callback);
        client.no_method();
    }

    /**
     * Test of getClient method, of class WebSocketClientActionFactory.
     */
    @Test
    public void testGetClient_ProtocolException() {
        DemoClientInterface client = webSocketClientActionFactory.getClient(DemoClientInterface.class);
        //TODO
    }

    /**
     * Test of getClient method, of class WebSocketClientActionFactory.
     */
    @Test
    public void testGetClient_exception() {
        DemoClientInterface client = webSocketClientActionFactory.getClient(DemoClientInterface.class);
        final int waitSeconds = DEFAULT_WAIT_SECONDS;
        final String msg = "异常测试 ok!";
        client.exception(msg, new ResultCallback<RpcException>() {
            @Override
            public RpcException callback(Future<RpcException> result) {
                RpcException rpcEx = null;
                try {
                    rpcEx = result.get(waitSeconds, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    fail("Not happen", ex);
                }
                assertNotNull(rpcEx);
                assertEquals(rpcEx.getCause().getClass(), IllegalArgumentException.class);
                log.info("Received exception [{}].", rpcEx.getLocalizedMessage());
                return rpcEx;
            }
        });
    }

    /**
     * Test of getClient method, of class WebSocketClientActionFactory.
     */
    @Test
    public void testGetClient_sleep() {
        DemoClientInterface client = webSocketClientActionFactory.getClient(DemoClientInterface.class);
        //sleep 3s
        client.sleep(3000, new ResultCallback<Void>() {
            @Override
            public Void callback(Future<Void> result) {
                try {
                    //wait 1s timeout
                    result.get(1, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    assertEquals(ex.getClass(), TimeoutException.class);
                }
                return null;
            }
        });

        //sleep 1.5
        client.sleep(1500, new ResultCallback<Void>() {
            @Override
            public Void callback(Future<Void> result) {
                try {
                    //wait 5s timeout
                    result.get(5, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    fail("Not happen", ex);
                }
                return null;
            }
        });
    }
}
