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
package jrouter.rpc.router.client.websocket.util;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * {@code PooledObjectFactory} for {@code Session}.
 */
public class SessionPooledObjectFactory implements PooledObjectFactory<Session> {

    //ping message
    private static final ByteBuffer PING_MSG = ByteBuffer.wrap(new byte[0]);

    /** WebSocketContainer */
    private final WebSocketContainer webSocketContainer;

    /** Endpoint object */
    private final Object endpoint;

    /** websocket url */
    private final URI path;

    public SessionPooledObjectFactory(WebSocketContainer webSocketContainer, Object endpoint, URI path) {
        this.webSocketContainer = webSocketContainer;
        this.endpoint = endpoint;
        this.path = path;
    }

    @Override
    public PooledObject<Session> makeObject() throws Exception {
        Session session = webSocketContainer.connectToServer(endpoint, path);
        return new DefaultPooledObject<>(session);
    }

    @Override
    public boolean validateObject(PooledObject<Session> p) {
        Session session = p.getObject();
        if (session == null || !session.isOpen()) {
            return false;
        }
        try {
            session.getAsyncRemote().sendPing(PING_MSG);
        } catch (IOException | IllegalArgumentException ex) {
            //ignore
            return false;
        }
        return true;
    }

    @Override
    public void destroyObject(PooledObject<Session> p) throws Exception {
        Session session = p.getObject();
        if (session != null) {
            session.close();
        }
    }

    @Override
    public void activateObject(PooledObject<Session> p) throws Exception {
        //No Op
    }

    @Override
    public void passivateObject(PooledObject<Session> p) throws Exception {
        //No Op
    }

}
