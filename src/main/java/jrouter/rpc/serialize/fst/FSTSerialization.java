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
package jrouter.rpc.serialize.fst;

import jrouter.rpc.serialize.ObjectSerialization;
import org.nustaq.serialization.FSTConfiguration;

/**
 * FSTSerialization.
 */
public class FSTSerialization implements ObjectSerialization {

    private static final ThreadLocal<FSTConfiguration> FST_CONFIGURATION = new ThreadLocal() {

        @Override
        public FSTConfiguration initialValue() {
            return FSTConfiguration.createDefaultConfiguration();
        }
    };

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        return FST_CONFIGURATION.get().asByteArray(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) {
        if (bytes == null) {
            return null;
        }
        return (T) FST_CONFIGURATION.get().asObject(bytes);
    }

    @Override
    public byte getType() {
        return 5;
    }

}
