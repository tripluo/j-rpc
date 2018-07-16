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
package net.jrouter.rpc.serialize;

/**
 * Basic interface serialization and deserialization of Objects to byte arrays (binary data). It is recommended that
 * implementations are designed to handle null objects/empty arrays on serialization and deserialization side.
 */
public interface ObjectSerialization {

    /**
     * Serialize the given object to binary data.
     *
     * @param t object to serialize
     *
     * @return the equivalent binary data
     */
    byte[] serialize(Object t);

    /**
     * Deserialize an object from the given binary data.
     *
     * @param <T> Object class type
     * @param bytes object binary representation
     * @param clz specified class type
     *
     * @return the equivalent object instance
     */
    <T> T deserialize(byte[] bytes, Class<T> clz);

    /**
     * Serialization type.
     *
     * @return Serialization type.
     */
    byte getType();
}
