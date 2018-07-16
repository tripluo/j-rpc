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
package net.jrouter.rpc;

/**
 * 用此异常或其子异常来标识特定的RPC异常信息。
 */
public class RpcException extends RuntimeException {

    /**
     * Empty Constructor for Serialization.
     */
    public RpcException() {
        super();
    }

    /**
     * 构造一个包含指定详细消息的RpcException。
     *
     * @param message 详细消息。
     */
    public RpcException(String message) {
        super(message);
    }

    /**
     * 构造一个包含指定原因的的RpcException。
     *
     * @param cause 异常原因。
     */
    public RpcException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个包含指定详细消息和原因的RpcException。
     *
     * @param message 详细消息。
     * @param cause 异常原因。
     */
    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

}
