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
package jrouter.rpc.router.bytecode.javassist;

import jrouter.ActionFactory;
import static jrouter.rpc.router.bytecode.javassist.JavassistObjectFactory.PROXY_CLASS_ACTION_FACTORY_FIELD_NAME;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.util.Assert;

/**
 * 提供基于 springframework 的{@code BeanDefinitionRegistry}注册 Bean 实例。
 *
 * @see BeanDefinitionRegistryPostProcessor
 */
@Slf4j
public abstract class SpringJavassistObjectFactory extends JavassistObjectFactory {

    /** ConfigurableListableBeanFactory */
    @lombok.Setter
    private ConfigurableListableBeanFactory beanFactory;

    /** ConfigurableListableBeanFactory */
    @lombok.Setter
    private BeanDefinitionRegistry registry;

    /** BeanNameGenerator */
    private final BeanNameGenerator beanNameGenerator;

    /**
     * Constructor.
     *
     * @param actionFactory ActionFactory object.
     */
    public SpringJavassistObjectFactory(ActionFactory<String> actionFactory) {
        super(actionFactory);
        beanNameGenerator = new AnnotationBeanNameGenerator();
    }

    @Override
    protected <T> T newInstance(Class<T> originalInterface, Class<T> proxiedClass) throws Exception {
        Assert.notNull(registry, "BeanDefinitionRegistry is required.");
        Assert.notNull(beanFactory, "ConfigurableListableBeanFactory is required.");
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(proxiedClass);
        builder.addPropertyValue(PROXY_CLASS_ACTION_FACTORY_FIELD_NAME, getActionFactory());
        builder.addPropertyValue(PROXY_CLASS_TARGET_CLASS_FIELD_NAME, originalInterface);
        BeanDefinition definition = builder.getBeanDefinition();
        String beanName = beanNameGenerator.generateBeanName(definition, registry);
        registry.registerBeanDefinition(beanName, definition);
        return beanFactory.getBean(beanName, proxiedClass);
    }

}
