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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import net.jrouter.rpc.exception.SerializationException;

/**
 * Java Native Serialization.
 */
public class JavaSerialization implements ObjectSerialization {

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
        } catch (Exception ex) {
            throw new SerializationException("Failed to serialize object of type: " + obj.getClass(), ex);
        }
        return baos.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) {
        if (bytes == null) {
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return (T) ois.readObject();
        } catch (ClassNotFoundException ex) {
            throw new SerializationException("Failed to deserialize object type", ex);
        } catch (Exception ex) {
            throw new SerializationException("Failed to deserialize object", ex);
        }
    }

    @Override
    public byte getType() {
        return 0;
    }
}
