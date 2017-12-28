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
package jrouter.rpc.spring.websocket.config;

import java.util.HashMap;
import java.util.Map;
import jrouter.rpc.DemoService;
import jrouter.rpc.router.result.websocket.WebSocketResult;
import jrouter.rpc.router.server.websocket.WebSocketServerActionFactory;
import jrouter.spring.SpringObjectFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringWebSocketServerActionFactoryConfiguration.
 */
@Configuration
@Slf4j
public class SpringWebSocketServerActionFactoryConfiguration {

    @Bean(destroyMethod = "clear")
    WebSocketServerActionFactory springWebSocketServerActionFactory(ApplicationContext applicationContext) {
        Map<String, Object> props = new HashMap<>(2);
        props.put("defaultResultType", WebSocketResult.WEB_SOCKET);
        props.put("objectFactory", new SpringObjectFactory(applicationContext));
        WebSocketServerActionFactory serverActionFactory = new WebSocketServerActionFactory(props);
        serverActionFactory.addActions(DemoService.class);
        serverActionFactory.addResultTypes(new WebSocketResult());
        return serverActionFactory;
    }
}
