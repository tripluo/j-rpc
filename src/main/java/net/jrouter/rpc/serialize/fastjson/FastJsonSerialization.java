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
package net.jrouter.rpc.serialize.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import net.jrouter.rpc.serialize.ObjectSerialization;

/**
 * FastJsonSerialization.
 */
public class FastJsonSerialization implements ObjectSerialization {

    @lombok.Setter
    @lombok.Getter
    private FastJsonConfig fastJsonConfig = new FastJsonConfig();

    /**
     * Constructor.
     */
    public FastJsonSerialization() {
        ParserConfig parserConfig = new ParserConfig();
        parserConfig.setAutoTypeSupport(true);
        fastJsonConfig.setParserConfig(parserConfig);
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.SkipTransientField,
                SerializerFeature.WriteClassName
        );
        fastJsonConfig.setParserConfig(parserConfig);

    }

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        return JSON.toJSONBytes(obj, fastJsonConfig.getSerializeConfig(), fastJsonConfig.getSerializeFilters(),
                JSON.DEFAULT_GENERATE_FEATURE, fastJsonConfig.getSerializerFeatures());
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) {
        if (bytes == null) {
            return null;
        }
        return JSON.parseObject(new String(bytes, fastJsonConfig.getCharset()), clz, fastJsonConfig.getParserConfig(),
                fastJsonConfig.getFeatures());
    }

    @Override
    public byte getType() {
        return 4;
    }
}
