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

import java.util.ArrayList;
import java.util.List;
import net.jrouter.rpc.util.Constants;

/**
 * 定义包含{@code String}类型路径的RPC协议。
 */
@lombok.Getter
@lombok.Setter
public class RpcProtocol implements Protocol<String>, Cloneable {

    /**
     * 唯一标识记录集合。
     */
    private List<Long> records = new ArrayList<>(2);

    /**
     * 调用路径。
     */
    private String path;

    /**
     * 调用参数。
     */
    private Object[] parameters;

    /**
     * 设置异步回调返回值（可根据此值类型定制是否需要回调）.
     */
    private Object result;

    /**
     * 序列化类型。
     */
    private byte serializationType = Constants.DEFAULT_OBJECT_SERIALIZATION.getType();

    @Override
    public void setId(Long id) {
        records.add(id);
    }

    @Override
    public Long getId() {
        return records.isEmpty() ? null : records.get(0);
    }

    @Override
    public String toString() {
        return "RpcProtocol{" + "ids=" + records + ", path=" + path + ", parameters=[" + (parameters == null ? "" : "(" + parameters.length + ")") + "], result=" + (result == null ? "" : result.getClass().getName()) + '}';
    }

    @Override
    public RpcProtocol clone() throws CloneNotSupportedException {
        return (RpcProtocol) super.clone();
    }
}
