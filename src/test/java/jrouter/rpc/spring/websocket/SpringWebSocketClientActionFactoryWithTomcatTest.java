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
package jrouter.rpc.spring.websocket;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import jrouter.rpc.DemoClientInterface2;
import jrouter.rpc.TomcatBaseTest;
import jrouter.rpc.protocol.Protocol;
import jrouter.rpc.router.ResultCallback;
import jrouter.rpc.router.client.RpcClientContext;
import jrouter.rpc.spring.websocket.config.SpringWebSocketClientActionFactoryConfiguration;
import jrouter.rpc.spring.websocket.config.SpringWebSocketServerActionFactoryConfiguration;
import lombok.extern.slf4j.Slf4j;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * SpringWebSocketClientActionFactoryWithTomcatTest.
 *
 * @see SpringWebSocketServerActionFactoryConfiguration
 * @see SpringWebSocketClientActionFactoryConfiguration
 */
@Slf4j
public class SpringWebSocketClientActionFactoryWithTomcatTest extends TomcatBaseTest {

    //3s
    private static final int DEFAULT_WAIT_SECONDS = 3;

    private DemoClientInterface2 demoClientInterface2;

    @BeforeTest
    public void beforeTestWithTomcat() throws Exception {
        SERVER_CONTAINER.addEndpoint(SpringDemoServerEndpoint.class);
        demoClientInterface2 = WEB_APPLICATION_CONTEXT.getBean(DemoClientInterface2.class);
        assertNotNull(demoClientInterface2);
    }

    /**
     * Test of Client of springframework.
     */
    @Test
    public void testGetClient() {
        final int waitSeconds = DEFAULT_WAIT_SECONDS;
        ResultCallback callback = null;

        //test echo
        final String msg1 = "Hello 你好！";
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
        demoClientInterface2.echo(msg1);
    }

    /**
     * Test of getClient method, of class WebSocketClientActionFactory.
     */
    @Test
    public void testGetClient_complex() {
        final int waitSeconds = DEFAULT_WAIT_SECONDS;
        final String msg = "复杂对象测试!";
        demoClientInterface2.getProtocol(99, msg, Arrays.asList("aab", msg), new ResultCallback<Protocol<String>>() {
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
}
