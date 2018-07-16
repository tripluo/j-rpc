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
package net.jrouter.rpc.serialize.jackson;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.jrouter.rpc.exception.SerializationException;
import net.jrouter.rpc.serialize.ObjectSerialization;

/**
 * JacksonSerialization.
 */
public class JacksonSerialization implements ObjectSerialization {

    @lombok.Setter
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor.
     */
    public JacksonSerialization() {
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException ex) {
            throw new SerializationException("Could not write JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, clz);
        } catch (Exception ex) {
            throw new SerializationException("Could not read JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    public byte getType() {
        return 2;
    }

}
