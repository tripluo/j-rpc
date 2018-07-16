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
package net.jrouter.rpc.transport.http.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import net.jrouter.rpc.router.RpcActionFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Sample WebSocketEndpoint.
 */
@ClientEndpoint
@ServerEndpoint("/")
@Slf4j
public abstract class WebSocketEndpoint {

    @lombok.Getter
    @lombok.Setter
    private RpcActionFactory<Session> rpcActionFactory;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        log.info("Session [{}] on open.", session.getId());
    }

    /**
     * @see javax.websocket.OnMessage
     */
    @OnMessage
    public void onBinaryMessage(Session session, byte[] messages, boolean isLast) {
        log.info("Receiving BinaryMessage {} : {}, last : {}", session.getId(), messages.length, isLast);
        getRpcActionFactory().onMessage(messages, session);
    }

//    @OnMessage
    public void onStreamMessage(Session session, InputStream input) throws IOException {
        log.info("Receiving StreamMessage {} : {}", session.getId(), input.available());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int n;
        while (-1 != (n = input.read(bytes))) {
            output.write(bytes, 0, n);
        }
        getRpcActionFactory().onMessage(output.toByteArray(), session);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info("Session [{}] on close for [{}].", session.getId(), closeReason);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        log.error("Session [{}] on error for [{}].", session.getId(), thr);
    }

    @OnMessage
    public void onPongMessage(PongMessage pm) {
        // NO-OP
        log.debug("Receiving pong : {}", pm.getApplicationData());
    }
}
