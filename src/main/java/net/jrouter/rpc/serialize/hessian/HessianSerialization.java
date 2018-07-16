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
package net.jrouter.rpc.serialize.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import net.jrouter.rpc.exception.SerializationException;
import net.jrouter.rpc.serialize.ObjectSerialization;

/**
 * Use Hessian for serialization and deserialization.
 */
public class HessianSerialization implements ObjectSerialization {

    @lombok.Setter
    private SerializerFactory serializerFactory;

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream bOut = new ByteArrayOutputStream(128);
        Hessian2Output output = new Hessian2Output(bOut);
        if (serializerFactory != null) {
            output.setSerializerFactory(serializerFactory);
        }
        try {
            output.writeObject(obj);
            output.close();
        } catch (Exception ex) {
            throw new SerializationException("Failed to serialize object of type: " + obj.getClass(), ex);
        }

        return bOut.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
        Hessian2Input input = new Hessian2Input(bIn);
        if (serializerFactory != null) {
            input.setSerializerFactory(serializerFactory);
        }
        try {
            return (T) input.readObject(clz);
        } catch (Exception ex) {
            throw new SerializationException("Failed to deserialize object of type: " + clz, ex);
        }
    }

    @Override
    public byte getType() {
        return 1;
    }
}
