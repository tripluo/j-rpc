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
package jrouter.rpc.serialize.hessian;

import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.SerializerFactory;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.FactoryBean;

/**
 * HessianSerialization FactoryBean.
 */
public class HessianSerializationFactoryBean implements FactoryBean<SerializerFactory> {

    private List<AbstractSerializerFactory> serializerFactory = Collections.EMPTY_LIST;

    @Override
    public SerializerFactory getObject() throws Exception {
        SerializerFactory sf = SerializerFactory.createDefault();
        sf.setAllowNonSerializable(true);
        sf.setSendCollectionType(true);
        for (AbstractSerializerFactory f : serializerFactory) {
            sf.addFactory(f);
        }
        return sf;
    }

    @Override
    public Class<?> getObjectType() {
        return SerializerFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setSerializerFactory(List<AbstractSerializerFactory> serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

}