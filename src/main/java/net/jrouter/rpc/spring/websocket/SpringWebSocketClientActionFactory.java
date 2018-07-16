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
package net.jrouter.rpc.spring.websocket;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.websocket.Session;
import jrouter.JRouterException;
import net.jrouter.rpc.RpcException;
import net.jrouter.rpc.router.bytecode.javassist.SpringJavassistObjectFactory;
import net.jrouter.rpc.router.client.websocket.WebSocketClientActionFactory;
import jrouter.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * 提供基于 springframework 的{@code BeanDefinitionRegistryPostProcessor} 加载 Client 端接口生成类。
 */
@Slf4j
public class SpringWebSocketClientActionFactory extends WebSocketClientActionFactory implements
        BeanDefinitionRegistryPostProcessor {

    //BeanDefinitionRegistry
    private BeanDefinitionRegistry registry;

    //BeanDefinitionRegistry
    private ConfigurableListableBeanFactory beanFactory;

    /* Client 端接口集合 */
    @lombok.Setter
    private List<?> clientInterfaces = Collections.EMPTY_LIST;

    //added flag
    private final AtomicBoolean _interfaces_added = new AtomicBoolean(false);

    //SpringJavassistObjectFactory
    private final SpringJavassistObjectFactory clientObjectFactory = new SpringJavassistObjectFactory(this) {

        @Override
        public String parsePath(Class<?> targetClass, Method method) {
            return SpringWebSocketClientActionFactory.this.parsePath(targetClass, method);
        }
    };

    /**
     * Constructor.
     *
     * @param pool Session pool.
     * @param properties Properties.
     */
    public SpringWebSocketClientActionFactory(ObjectPool<Session> pool, Properties properties) {
        super(pool, properties);
    }

    @Override
    public void addActions(Object obj) {
        Class<?> clientInterface = null;
        //clinet only accpets interface
        if (obj instanceof Class && ((clientInterface = (Class) obj)).isInterface()) {
            Class<? extends Annotation> clientAnnotatedClass = getClientAnnotatedClass();
            if (clientAnnotatedClass != null && !clientInterface.isAnnotationPresent(clientAnnotatedClass)) {
                throw new JRouterException(String.format("Client interface [%s] should has @%s", clientInterface, clientAnnotatedClass));
            }
            //因为需代理方法参数，方法名称需唯一
            //create dynamic interface
            Object exist = getClients().put(clientInterface, clientObjectFactory.newInstance(clientInterface));
            if (exist != null) {
                throw new JRouterException("Client [" + clientInterface + "] already exists.");
            }
        } else {
            log.warn("Only accepts interface types, ignore [{}]", obj == null ? "null" : obj.getClass());
        }
    }

    /**
     * 自动加载接口集合。
     */
    private void registerClientInterfaces() {
        if (registry != null && beanFactory != null && _interfaces_added.compareAndSet(false, true)) {
            clientObjectFactory.setRegistry(registry);
            clientObjectFactory.setBeanFactory(beanFactory);
            clientInterfaces.forEach((client) -> {
                if (client instanceof String) {
                    try {
                        addActions(ClassUtil.loadClass((String) client));
                    } catch (ClassNotFoundException ex) {
                        //rethrow
                        throw new RpcException(ex);
                    }
                } else {
                    addActions(client);
                }
            });
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
        registerClientInterfaces();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        registerClientInterfaces();
    }
}
