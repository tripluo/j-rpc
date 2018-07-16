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

import java.util.Map;
import jrouter.ActionInvocation;
import jrouter.annotation.Dynamic;
import net.jrouter.rpc.RpcException;
import net.jrouter.rpc.protocol.Protocol;

/**
 * 扩展{@code ActionInvocation<String>}，返回 RPC 常用参数的接口。
 *
 * @param <S> RPC Session.
 */
@Dynamic
public interface RpcActionInvocation<S> extends ActionInvocation<String> {

    /**
     * Gets the {@code Protocol} object.
     *
     * @return the Protocol object.
     */
    Protocol<String> getProtocol();

    /**
     * Gets the RPC session object.
     *
     * @return the RPC session object.
     */
    S getSession();

    /**
     * 返回{@link RpcActionFactory}。
     *
     * @return RpcActionFactory.
     */
    @Override
    RpcActionFactory<S> getActionFactory();

    /**
     * 返回调用过程中发生的RPC异常；无异常返回 null。
     *
     * @return Exception对象.
     */
    RpcException getRpcException();

    /**
     * Get Invocation Context Map.
     *
     * @return the Context Map.
     */
    Map<String, Object> getContextMap();
}
