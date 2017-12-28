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
package jrouter.rpc.router.result.websocket;

import java.nio.ByteBuffer;
import javax.websocket.SendHandler;
import javax.websocket.Session;
import jrouter.annotation.ResultType;
import jrouter.rpc.protocol.Protocol;
import jrouter.rpc.protocol.RpcProtocol;
import jrouter.rpc.support.IdWorker;
import lombok.extern.slf4j.Slf4j;
import jrouter.rpc.router.RpcActionInvocation;

/**
 * 处理返回结果，转换为{@code Protocol}对象，异步传输 websocket 消息。
 *
 * @param <T> websocket 模型类
 */
@Slf4j
public class WebSocketResult<T extends WebSocketModel> {

    public static final String WEB_SOCKET = "web_socket";

    /**
     * {@code Long}型Id生成器。
     */
    @lombok.Getter
    @lombok.Setter
    private IdWorker idGenerator = new IdWorker(0, 0);

    /**
     * Return result using websocket async sendBinary method.
     *
     * @param invocation RpcActionInvocation object.
     *
     * @return The result of return.
     */
    @ResultType(type = WEB_SOCKET)
    public Object callback(RpcActionInvocation<Session> invocation) {
        Protocol<String> protocol = invocation.getProtocol();
        Object res = invocation.getInvokeResult();
        WebSocketModel model = null;
        if (res instanceof WebSocketModel) {
            model = (WebSocketModel) res;
            protocol.setResult(model.getData());
        } else {
            protocol.setResult(res);
        }
        Session session = invocation.getSession();
        if (session != null && session.isOpen()) {
            SendHandler sendHandler = null;
            if (model != null) {
                sendHandler = model.getSendHandler();
            }
            //add record
            protocol.getRecords().add(idGenerator.nextId());
            //TODO
            //clear protocol's received parameters
            if (protocol instanceof RpcProtocol) {
                ((RpcProtocol) protocol).setParameters(null);
            }
            if (sendHandler == null) {
                session.getAsyncRemote().sendBinary(ByteBuffer.wrap(invocation.getActionFactory().getObjectSerialization().serialize(protocol)));
            } else {
                session.getAsyncRemote().sendBinary(ByteBuffer.wrap(invocation.getActionFactory().getObjectSerialization().serialize(protocol)), sendHandler);
            }
        } else {
            log.error("Can't get websocket session or session is not open.");
        }
        return res;
    }
}
