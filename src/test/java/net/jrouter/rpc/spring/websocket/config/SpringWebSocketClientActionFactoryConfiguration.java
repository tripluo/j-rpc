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
package net.jrouter.rpc.spring.websocket.config;

import java.net.URI;
import java.util.Arrays;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import static net.jrouter.rpc.TomcatBaseTest.*;
import net.jrouter.rpc.router.client.websocket.DemoClientEndpoint;
import net.jrouter.rpc.router.client.websocket.util.SessionPooledObjectFactory;
import net.jrouter.rpc.spring.websocket.SpringDemoServerEndpoint;
import net.jrouter.rpc.spring.websocket.SpringWebSocketClientActionFactory;
import lombok.extern.slf4j.Slf4j;
import net.jrouter.rpc.router.client.websocket.WebSocketClientActionFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringWebSocketClientActionFactoryConfiguration.
 */
@Configuration
@Slf4j
public class SpringWebSocketClientActionFactoryConfiguration {

    @Bean(destroyMethod = "clear")
    static SpringWebSocketClientActionFactory springWebSocketClientActionFactory(DemoClientEndpoint demoClientEndpoint,
            ObjectPool<Session> sessionPool) {
        WebSocketClientActionFactory.Properties properties = new WebSocketClientActionFactory.Properties();
        SpringWebSocketClientActionFactory factory = new SpringWebSocketClientActionFactory(sessionPool, properties);
        //add client interfaces
        factory.setClientInterfaces(Arrays.asList(net.jrouter.rpc.DemoClientInterface2.class));
        //set RpcActionFactory
        demoClientEndpoint.setRpcActionFactory(factory);
        return factory;
    }

    @Bean
    DemoClientEndpoint demoClientEndpoint() {
        return new DemoClientEndpoint();
    }

    @Bean
    ObjectPool<Session> sessionPool(DemoClientEndpoint clientEndpoint) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String demoWebSocketURL = "ws://localhost:" + getPort() + CONTEXT_PATH + SpringDemoServerEndpoint.SERVER_URL;
        log.info("Connecting to Server : {}", demoWebSocketURL);

        //pool config
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(10000);
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);
        return new GenericObjectPool<>(
                new SessionPooledObjectFactory(container, clientEndpoint, new URI(demoWebSocketURL)), poolConfig);
    }
}
