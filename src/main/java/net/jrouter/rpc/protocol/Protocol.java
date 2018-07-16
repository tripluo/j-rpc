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
package net.jrouter.rpc.protocol;

import java.io.Serializable;
import net.jrouter.id.Id;
import net.jrouter.id.Recordable;

/**
 * 包含指定路径、ID、参数、返回结果的RPC协议。
 *
 * @param <P> Path type.
 */
public interface Protocol<P> extends Id<Long>, Recordable<Long>, Serializable {

    /**
     * 定义RPC路径。
     *
     * @return RPC地址。
     */
    P getPath();

    /**
     * RPC参数。
     *
     * @return RPC参数。
     */
    Object[] getParameters();

    /**
     * 设置返回结果。
     *
     * @param result 返回结果。
     */
    void setResult(Object result);

    /**
     * 获取返回结果。
     *
     * @return 返回结果。
     */
    Object getResult();

    /**
     * 获取序列化类型。
     *
     * @return 序列化类型。
     */
    byte getSerializationType();

    /**
     * 设置序列化类型。
     *
     * @param serializationType 序列化类型。
     */
    void setSerializationType(byte serializationType);
}
