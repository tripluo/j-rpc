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
package net.jrouter.rpc.transport.http.websocket.jetty;

import net.jrouter.rpc.router.RpcActionFactory;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * TODO.
 */
@Slf4j
@WebSocket(maxBinaryMessageSize = 1024 * 1024)
public abstract class JettyWebSocket {

    @lombok.Getter
    @lombok.Setter
    private RpcActionFactory<Session> rpcActionFactory;

    @OnWebSocketConnect
    public void onConnect(Session session) {
        log.info("Session [{}] on connect.", session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        log.info("Session [{}] on close for [{}].", session, statusCode + ":" + reason);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, byte[] messages, int offset, int length) {
        log.info("Receiving BinaryMessage {} : {}, last : {}", session, messages.length, length);
        getRpcActionFactory().onMessage(messages, session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        log.error("Session [{}] on error for [{}].", session, error);
    }

}
