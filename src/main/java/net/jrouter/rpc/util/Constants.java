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
package net.jrouter.rpc.util;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import net.jrouter.rpc.serialize.JavaSerialization;
import net.jrouter.rpc.serialize.hessian.HessianSerialization;
import net.jrouter.rpc.serialize.ObjectSerialization;
import lombok.extern.slf4j.Slf4j;

/**
 * Constants.
 */
@Slf4j
public final class Constants {

    /** 检测是否引入Hessian */
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
            log.warn("Can't find Hessian jar, load [{} - {}] as default ObjectSerialization Constant.",
                    DEFAULT_OBJECT_SERIALIZATION.getType(), DEFAULT_OBJECT_SERIALIZATION);
        }
    }

    /**
     * Supported Object Serializations.
     */
    public static final Map<Byte, ObjectSerialization> SUPPORTED_OBJECT_SERIALIZATIONS = new HashMap<>(4);

    static {
        ServiceLoader.load(ObjectSerialization.class).forEach((ser) -> {
            ObjectSerialization os = null;
            try {
                //validate serialization
                ser.serialize("");
                os = ser;
            } catch (Throwable e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error loading ObjectSerialization [" + ser + "]", e);
                }
                //ignore
            }
            if (os != null) {
                if (os.getClass() == DEFAULT_OBJECT_SERIALIZATION.getClass()) {
                    os = DEFAULT_OBJECT_SERIALIZATION;
                }
                ObjectSerialization exist = SUPPORTED_OBJECT_SERIALIZATIONS.put(os.getType(), os);
                if (exist != null) {
                    if (os.getClass() != exist.getClass()) {
                        throw new IllegalArgumentException(
                                String.format("Duplicated type [%s] of [%s] and [%s]", os.getType(), os, exist));
                    }
                }
                if (log.isInfoEnabled()) {
                    log.info("Loaded ObjectSerialization [{} - {}]", os.getType(), os);
                }
            }
        });
    }
}
