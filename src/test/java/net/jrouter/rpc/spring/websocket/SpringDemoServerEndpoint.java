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
package net.jrouter.rpc.spring.websocket;

import javax.annotation.Resource;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import net.jrouter.rpc.router.RpcActionFactory;
import net.jrouter.rpc.transport.http.websocket.WebSocketEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.server.standard.SpringConfigurator;

/**
 * SpringDemoServerEndpoint.
 */
@Service
@ServerEndpoint(value = SpringDemoServerEndpoint.SERVER_URL, configurator = SpringConfigurator.class)
@Slf4j
public class SpringDemoServerEndpoint extends WebSocketEndpoint {

    public static final String SERVER_URL = "/websocket/spring/demo";

    @Resource(name = "springWebSocketServerActionFactory")
    @Override
    public void setRpcActionFactory(RpcActionFactory<Session> rpcActionFactory) {
        super.setRpcActionFactory(rpcActionFactory);
    }

}
