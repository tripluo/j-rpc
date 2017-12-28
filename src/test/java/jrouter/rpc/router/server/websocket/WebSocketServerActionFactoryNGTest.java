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
package jrouter.rpc.router.server.websocket;

import java.util.HashMap;
import java.util.Map;
import jrouter.rpc.DemoService;
import jrouter.rpc.TomcatBaseTest;
import jrouter.rpc.router.result.websocket.WebSocketResult;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

/**
 * WebSocketServerActionFactoryNGTest.
 */
@Slf4j
public abstract class WebSocketServerActionFactoryNGTest extends TomcatBaseTest {

    //singlton
    private static WebSocketServerActionFactory WEBSOCKET_SERVER_ACTION_FACTORY;

    @BeforeTest
    public static final void setUpWebSocketServerActionFactory() throws Exception {
        Map<String, Object> props = new HashMap<>(2);
//        props.put("defaultInterceptorStack", DefaultInterceptorStack.EXCEPTION_INTERCEPTOR_STACK);
        props.put("defaultResultType", WebSocketResult.WEB_SOCKET);
        WEBSOCKET_SERVER_ACTION_FACTORY = new WebSocketServerActionFactory(props);

//        webSocketServerActionFactory.addInterceptors(ExceptionInterceptor.class);
//        webSocketServerActionFactory.addInterceptorStacks(DefaultInterceptorStack.class);
        //websocket result
        WEBSOCKET_SERVER_ACTION_FACTORY.addResultTypes(new WebSocketResult());
        WEBSOCKET_SERVER_ACTION_FACTORY.addActions(DemoService.class);

        DemoServerEndpoint.DEMO_INSTANCE = WEBSOCKET_SERVER_ACTION_FACTORY;
        //add server endpoint, only once
        SERVER_CONTAINER.addEndpoint(DemoServerEndpoint.class);
    }

    @AfterTest
    public static final void tearDownWebSocketServerActionFactory() throws Exception {
        if (WEBSOCKET_SERVER_ACTION_FACTORY != null) {
            WEBSOCKET_SERVER_ACTION_FACTORY.clear();
        }
    }

}
