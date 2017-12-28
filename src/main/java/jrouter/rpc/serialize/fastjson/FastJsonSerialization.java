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
package jrouter.rpc.serialize.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import jrouter.rpc.serialize.ObjectSerialization;

/**
 * FastJsonSerialization.
 */
public class FastJsonSerialization implements ObjectSerialization {

    @lombok.Setter
    @lombok.Getter
    private FastJsonConfig fastJsonConfig = new FastJsonConfig();

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        return JSON.toJSONBytes(obj, fastJsonConfig.getSerializeConfig());
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) {
        if (bytes == null) {
            return null;
        }
        return JSON.parseObject(bytes, 0, bytes.length, fastJsonConfig.getCharset(), clz, fastJsonConfig.getFeatures());
    }

    @Override
    public byte getType() {
        return 4;
    }
}
