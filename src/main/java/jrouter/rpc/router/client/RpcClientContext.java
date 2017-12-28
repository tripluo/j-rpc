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
package jrouter.rpc.router.client;

import jrouter.rpc.router.ResultCallback;

/**
 * 使用 {@link ThreadLocal} 变量存取变量，达到减少方法参数的目的。
 *
 * @see WebSocketClientActionFactory#invokeAction
 */
public class RpcClientContext {

    /** Thread Safe */
    private static final ThreadLocal<RpcClientContext> THREAD_LOCAL = new ThreadLocal<RpcClientContext>() {

        @Override
        protected RpcClientContext initialValue() {
            return new RpcClientContext();
        }

    };

    /** 记录当前调用方法（多个调用方法覆盖）对应的回调方法 */
    @lombok.Getter
    @lombok.Setter
    private ResultCallback resultCallback;

    /**
     * Constructor.
     */
    private RpcClientContext() {
    }

    /**
     * 获取当前线程副本中的ThreadLocalContext。
     *
     * @return 前线程副本中的ThreadLocalContext。
     */
    public static RpcClientContext get() {
        return THREAD_LOCAL.get();
    }

    /**
     * 移除前线程副本中的ThreadLocalContext。
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
