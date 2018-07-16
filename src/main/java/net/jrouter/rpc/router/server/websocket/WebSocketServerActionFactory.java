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
package net.jrouter.rpc.router.server.websocket;

import javax.websocket.Session;
import net.jrouter.rpc.RpcException;
import net.jrouter.rpc.protocol.Protocol;
import net.jrouter.rpc.router.server.RpcServerActionFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 提供接收 WebSocket 消息，基于{@code String}型路径调用的{@code RpcActionFactory}实现。
 */
@Slf4j
public class WebSocketServerActionFactory extends RpcServerActionFactory<Session> {

    /**
     * Constructor.
     *
     * @param properties Properties.
     */
    public WebSocketServerActionFactory(Properties properties) {
        super(properties);
    }

    @Override
    protected void handleError(Protocol<String> protocol, Session session, Throwable t) {
        if (protocol != null) {
            log.error("Exception occured when handling rpc messages : " + protocol, t);
            //TODO　exception serialization
            if (t instanceof RpcException) {
                protocol.setResult(t);
            } else {
                protocol.setResult(new RpcException(t));
            }
            session.getAsyncRemote().sendBinary(getObjectSerialization().serializeByteBuffer(protocol));
        }
    }

}
