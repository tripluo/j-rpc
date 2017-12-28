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
package jrouter.rpc.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import jrouter.rpc.serialize.JavaSerialization;
import jrouter.rpc.serialize.hessian.HessianSerialization;
import jrouter.rpc.serialize.ObjectSerialization;
import lombok.extern.slf4j.Slf4j;

/**
 * Constants.
 */
@Slf4j
public final class Constants {

    /** 检测是否引入javassist */
    private static final boolean HESSIAN_SUPPORTED
            = jrouter.util.ClassUtil.loadClassQuietly("com.caucho.hessian.io.Hessian2Output") != null;

    /**
     * 默认 Hessian 提供序列化。
     */
    public static final ObjectSerialization DEFAULT_OBJECT_SERIALIZATION
            = HESSIAN_SUPPORTED
                    ? new HessianSerialization()
                    : new JavaSerialization();

    static {
        if (!HESSIAN_SUPPORTED) {
            log.warn("Can't find Hessian jar, load [{}] as default ObjectSerialization Constant.", DEFAULT_OBJECT_SERIALIZATION);
        }
    }

    /**
     * Supported Object Serializations.
     */
    public static final Map<Byte, ObjectSerialization> SUPPORTED_OBJECT_SERIALIZATIONS = new HashMap<>(4);

    static {
        Iterator<ObjectSerialization> it = ServiceLoader.load(ObjectSerialization.class).iterator();
        ObjectSerialization os = null;
        while (it.hasNext()) {
            os = null;
            ObjectSerialization next = null;
            try {
                next = it.next();
                //validate serialization
                next.serialize("");
                os = next;
            } catch (Throwable e) {
                log.debug("Error loading ObjectSerialization [" + next + "]", e);
                //ignore
            }
            if (os != null) {
                if (os.getClass() == DEFAULT_OBJECT_SERIALIZATION.getClass()) {
                    os = DEFAULT_OBJECT_SERIALIZATION;
                }
                ObjectSerialization exist = SUPPORTED_OBJECT_SERIALIZATIONS.put(os.getType(), os);
                if (exist != null) {
                    if (os.getClass() != exist.getClass()) {
                        throw new IllegalArgumentException("Duplicated type of [" + os + "] and [" + exist + "].");
                    }
                }
                log.info("Loaded ObjectSerialization [{}]", os);
            }
        }
    }
}
