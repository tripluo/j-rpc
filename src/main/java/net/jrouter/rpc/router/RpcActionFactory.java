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
package net.jrouter.rpc.router;

import jrouter.ActionFactory;
import net.jrouter.rpc.RpcException;
import net.jrouter.rpc.serialize.ObjectSerialization;

/**
 * 扩展{@code ActionFactory<String>}提供接收 RPC 消息，进而触发{@link #invokeAction(java.lang.Object, java.lang.Object...)}调用。
 *
 * @param <S> Session type.
 */
public interface RpcActionFactory<S> extends ActionFactory<String> {

    /**
     * 接收 RPC {@code byte[]}类型消息。
     *
     * @param <T> type.
     * @param protocol bytes data.
     * @param session RPC Session object.
     *
     * @return 调用结果。
     *
     * @throws RpcException 如果发生调用错误。
     */
    <T> T onMessage(byte[] protocol, S session) throws RpcException;

    /**
     * 返回对象的序列/反序列化提供者。
     *
     * @return 对象序列/反序列化提供者。
     */
    ObjectSerialization getObjectSerialization();
}
