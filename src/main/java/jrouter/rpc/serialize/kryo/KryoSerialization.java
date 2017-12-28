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
package jrouter.rpc.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import jrouter.rpc.serialize.ObjectSerialization;

/**
 * Kryo Serialization.
 */
public class KryoSerialization implements ObjectSerialization {

    // Setup ThreadLocal of Kryo instances
    private static final ThreadLocal<Kryo> KRYO = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.addDefaultSerializer(Arrays.asList().getClass(), ArraysArrayListSerializer.class);
            kryo.addDefaultSerializer(Collections.unmodifiableList(Collections.EMPTY_LIST).getClass(), ArraysArrayListSerializer.class);
            kryo.addDefaultSerializer(Collections.unmodifiableList(new LinkedList()).getClass(), ArraysArrayListSerializer.class);
            return kryo;
        }
    };

    /**
     * Serializer for {@code Arrays#ArrayList}.
     */
    static public class ArraysArrayListSerializer extends CollectionSerializer {

        @Override
        protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
            return new ArrayList();
        }

        @Override
        protected Collection createCopy(Kryo kryo, Collection original) {
            return new ArrayList();
        }

    }

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        Output output = new Output(1024, 1024 * 1024);
        KRYO.get().writeClassAndObject(output, obj);
        return output.getBuffer();

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) {
        if (bytes == null) {
            return null;
        }
        return (T) KRYO.get().readClassAndObject(new Input(bytes));
    }

    /**
     * Remove ThreadLocal kryo.
     */
    public static void remove() {
        KRYO.remove();
    }

    @Override
    public byte getType() {
        return 3;
    }
}
