/*
 * Copyright 2017 lluo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jrouter.rpc.exception;

import jrouter.rpc.RpcException;

/**
 * Generic exception indicating a serialization/deserialization error.
 */
public class SerializationException extends RpcException {

    /**
     * 构造一个包含指定详细消息的SerializationException。
     *
     * @param message 详细消息。
     */
    public SerializationException(String message) {
        super(message);
    }

    /**
     * 构造一个包含指定详细消息和原因的SerializationException。
     *
     * @param message 详细消息。
     * @param cause 异常原因。
     */
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
