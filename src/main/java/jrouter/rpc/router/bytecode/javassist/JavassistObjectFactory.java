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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import jrouter.ActionFactory;
import jrouter.ObjectFactory;
import jrouter.rpc.RpcException;
import jrouter.rpc.annotation.RpcConsumer;
import jrouter.util.CollectionUtil;
import jrouter.util.MethodUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 提供基于javassist的对象生成工厂，动态构建调用{@link ActionFactory#invokeAction}方法的接口代理对象。
 * 需实现抽象方法{@link #parsePath}，指明{@link ActionFactory#invokeAction}方法中的{@code String}类型路径参数。
 */
@Slf4j
public abstract class JavassistObjectFactory implements ObjectFactory {

    /** 接口代理类前缀 */
    private static final String PROXY_CLASS_PREFIX = JavassistObjectFactory.class.getPackage().getName() + ".";

    /** 接口代理类后缀 */
    private static final String PROXY_CLASS_SUFFIX = "$$JR_Proxy$$";

    /** 原有{@link Class}名称 */
    static final String PROXY_CLASS_TARGET_CLASS_FIELD_NAME = "_targetClass";

    /** {@link ActionFactory}对象属性名称 */
    static final String PROXY_CLASS_ACTION_FACTORY_FIELD_NAME = "_actionFactory";

    /** 计数器 */
    private static final AtomicInteger COUNTER = new AtomicInteger(0x10000);

    static {
//        CtClass.debugDump = System.getProperty("user.home") + "/Desktop" + "/javaDebug";
        ClassPool.getDefault().insertClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }

    /** {@code ActionFactory}对象 */
    @lombok.Getter
    private final ActionFactory<String> actionFactory;

    /**
     * 根据指定的ActionFactory构造JavassistObjectFactory对象。
     *
     * @param actionFactory ActionFactory对象。
     */
    public JavassistObjectFactory(ActionFactory<String> actionFactory) {
        this.actionFactory = actionFactory;
    }

    @Override
    public <T> T newInstance(Class<T> originalInterface) {
        if (!originalInterface.isInterface()) {
            log.warn("Only interface types can be proxied, no proxy at : {}", originalInterface);
            return null;
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating proxy interface at : {}", originalInterface);
            }
            Class<T> proxyClass = createInterfaceProxyClass(originalInterface).
                    toClass(originalInterface.getClassLoader(), originalInterface.getProtectionDomain());
            return newInstance(originalInterface, proxyClass);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    /**
     * 根据原有接口的{@code Class}类型和生成的{@code Class}类型生成一个新的对象实例。可用于子类继承已适配特定的容器。
     *
     *
     * @param <T> 生成对象实例的类型。
     * @param originalInterface 原有接口的{@code Class}类型。
     * @param proxiedClass 动态生成的{@code Class}类型。
     *
     * @return 新的对象实例。
     *
     * @throws java.lang.Exception if exception occurs.
     */
    protected <T> T newInstance(Class<T> originalInterface, Class<T> proxiedClass) throws Exception {
        T t = proxiedClass.newInstance();
        Field f = proxiedClass.getDeclaredField(PROXY_CLASS_ACTION_FACTORY_FIELD_NAME);
        f.setAccessible(true);
        f.set(t, actionFactory);
        f = proxiedClass.getDeclaredField(PROXY_CLASS_TARGET_CLASS_FIELD_NAME);
        f.setAccessible(true);
        f.set(t, originalInterface);
        return t;
    }

    /**
     * 根据接口类型构建{@link CtClass}对象。
     *
     * @param interfaceClass 接口类型。
     *
     * @return CtClass对象。
     *
     * @throws CannotCompileException when bytecode transformation has failed.
     * @throws NotFoundException when class is not found.
     */
    private CtClass createInterfaceProxyClass(Class<?> interfaceClass) throws CannotCompileException, NotFoundException {
        ClassPool classPool = ClassPool.getDefault();
        ClassClassPath classPath = new ClassClassPath(interfaceClass);
        classPool.insertClassPath(classPath);
        //import target class package
        classPool.importPackage(interfaceClass.getPackage().getName());

        //类前缀 + 16进制计数值
        CtClass proxyClass = classPool.makeClass(PROXY_CLASS_PREFIX + interfaceClass.getSimpleName() + PROXY_CLASS_SUFFIX + Integer.toHexString(COUNTER.getAndIncrement()));
        try {
            //特定接口/抽象类/类的调用
            proxyClass.addInterface(classPool.getCtClass(interfaceClass.getName()));
            //final
            proxyClass.setModifiers(Modifier.FINAL);
            proxyClass.addField(CtField.make("private Class " + PROXY_CLASS_TARGET_CLASS_FIELD_NAME + ";", proxyClass));
            proxyClass.addMethod(createSetMethod(proxyClass, PROXY_CLASS_TARGET_CLASS_FIELD_NAME, Class.class));
            proxyClass.addField(CtField.make("private jrouter.ActionFactory " + PROXY_CLASS_ACTION_FACTORY_FIELD_NAME + ";", proxyClass));
            proxyClass.addMethod(createSetMethod(proxyClass, PROXY_CLASS_ACTION_FACTORY_FIELD_NAME, ActionFactory.class));

            //avoid duplicated methods
            Set<String> methods = new HashSet<>(8);
            Method[] ms = interfaceClass.getDeclaredMethods();
            for (Method m : ms) {
                //interface method
                proxyClass.addMethod(createProxyMethod(proxyClass, interfaceClass, m));
                methods.add(MethodUtil.getSimpleMethod(m));
            }
            RpcConsumer consumer = interfaceClass.getAnnotation(RpcConsumer.class);
            if (consumer != null) {
                Class<?>[] proxyInterfaces = consumer.proxyInterfaces();
                if (CollectionUtil.isNotEmpty(proxyInterfaces)) {
                    for (Class<?> proxyInterface : proxyInterfaces) {
                        if (proxyInterface.isAssignableFrom(interfaceClass)) {
                            ms = proxyInterface.getDeclaredMethods();
                            for (Method m : ms) {
                                String methodSign = MethodUtil.getSimpleMethod(m);
                                if (!methods.contains(methodSign)) {
                                    //interface method
                                    proxyClass.addMethod(createProxyMethod(proxyClass, interfaceClass, m));
                                    methods.add(methodSign);
                                }
                            }
                        } else {
                            log.warn("[{}] is not a superinterface of [{}], ignore.", proxyInterface, interfaceClass);
                        }
                    }
                }
            }
        } finally {
            classPool.removeClassPath(classPath);
            classPool.clearImportedPackages();
            if (proxyClass != null) {
                proxyClass.detach();
            }
        }
        return proxyClass;
    }

    /**
     * 基于底层方法构建{@link CtMethod}方法。
     *
     * @param clazz 代理方法所在的 {@code CtClass} 类。
     * @param targetClass 底层方法所表示的 {@code Class} 对象。
     * @param method 底层方法。
     *
     * @return CtMethod方法。
     *
     * @throws CannotCompileException when bytecode transformation has failed.
     *
     * @see jrouter.ActionFactory#invokeAction
     */
    private CtMethod createProxyMethod(CtClass clazz, Class<?> targetClass, Method method) throws CannotCompileException {
        StringBuilder body = new StringBuilder("public ");
        Class<?> returnType = method.getReturnType();
        boolean voidMethod = void.class == returnType;
        if (voidMethod) {
            body.append("void ");
        } else {
            body.append(returnType.getCanonicalName()).append(" ");
        }
        body.append(method.getName()).append("(");
        //invoke begin
        Class<?>[] parameterTypes = method.getParameterTypes();
        int typeLength = 0;
        //no parameters method
        if ((typeLength = parameterTypes.length) != 0) {
            for (int i = 0; i < typeLength - 1; i++) {
                body.append(parameterTypes[i].getCanonicalName()).append(" ").append("p").append(i);
                body.append(",");
            }
            body.append(parameterTypes[typeLength - 1].getCanonicalName()).append(" ").append("p").append(typeLength - 1);
        }
        body.append("){");
        String path = parsePath(targetClass, method);
        body.append(PROXY_CLASS_ACTION_FACTORY_FIELD_NAME).append(".invokeAction(\"").append(path).append("\",$args);");
        body.append("return ").append(getReturnValue(returnType)).append(";}");
        return CtNewMethod.make(body.toString(), clazz);
    }

    /**
     * Create a set method.
     *
     * @param clazz 代理方法所在的 {@code CtClass} 类。
     * @param varName 变量名称。
     * @param type 变量类型。
     *
     * @return CtMethod方法。
     *
     * @throws CannotCompileException when bytecode transformation has failed.
     */
    private CtMethod createSetMethod(CtClass clazz, String varName, Class<?> type) throws CannotCompileException {
        char[] chars = varName.toCharArray();
        chars[0] = Character.toUpperCase(varName.charAt(0));
        StringBuilder body = new StringBuilder("public void set").append(chars).append("(");
        body.append(type.getCanonicalName()).append(" obj){");
        body.append("this.").append(varName).append("=obj;}");
        return CtNewMethod.make(body.toString(), clazz);
    }

    /**
     * 由指定的方法生成{@code String}型路径;此路径用于{@link ActionFactory#invokeAction}方法。
     *
     * @param targetClass 底层方法所表示的 {@code Class} 对象。
     * @param method 指定的底层方法。
     *
     * @return 用于{@link ActionFactory#invokeAction}的{@code String}型调用路径。
     */
    public abstract String parsePath(Class<?> targetClass, Method method);

    @Override
    public Class<?> getClass(Object obj) {
        if (obj != null) {
            try {
                Field f = obj.getClass().getDeclaredField(PROXY_CLASS_TARGET_CLASS_FIELD_NAME);
                f.setAccessible(true);
                return (Class) f.get(obj);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                log.error("Can't get class {}", obj);
            }
        }
        return null;
    }

    /**
     * 指定对象类型（包括基本类型）的默认（null/0/false）返回值。
     *
     * @param clazz 指定的对象类型。
     *
     * @return 返回值。
     */
    private String getReturnValue(Class<?> clazz) {
        if (void.class == clazz) {
            return "";
        }
        if (clazz.isPrimitive()) {
            return (clazz == boolean.class ? "false" : "($r)0");
        }
        return "null";
    }
}
