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
package jrouter.rpc.router.client.websocket;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import jrouter.JRouterException;
import jrouter.ObjectFactory;
import jrouter.annotation.Namespace;
import jrouter.impl.PathActionFactory;
import jrouter.rpc.RpcException;
import jrouter.rpc.annotation.RpcConsumer;
import jrouter.rpc.protocol.Protocol;
import jrouter.rpc.protocol.RpcProtocol;
import jrouter.rpc.router.ResultCallback;
import jrouter.rpc.router.bytecode.javassist.JavassistObjectFactory;
import jrouter.rpc.router.client.RpcClientActionFactory;
import jrouter.rpc.router.client.RpcClientContext;
import jrouter.rpc.router.impl.ProtocolSerialization;
import jrouter.rpc.support.IdGenerator;
import jrouter.rpc.support.IdWorker;
import jrouter.rpc.util.Constants;
import jrouter.util.CollectionUtil;
import jrouter.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;
import jrouter.rpc.serialize.ObjectSerialization;

/**
 * 提供接收 WebSocket 消息，基于{@code String}型路径调用的{@code RpcClientActionFactory}抽象类。
 */
@Slf4j
public class WebSocketClientActionFactory extends PathActionFactory implements RpcClientActionFactory<Session> {

    /**
     * {@code Protocol} 序列/反序列化工具。
     */
    private final ProtocolSerialization protocolSerialization;

    /**
     * {@code Long}型Id生成器。
     */
    @lombok.Getter
    @lombok.Setter
    private IdGenerator<Long> idGenerator = new IdWorker(0, 0);

    /**
     * 代理接口必须包含的注解类；默认为{@link RpcConsumer}。
     */
    @lombok.Getter
    @lombok.Setter
    private Class<? extends Annotation> clientAnnotatedClass = RpcConsumer.class;

    /**
     * 由{@code Class}生成的客户端代理对象映射。
     *
     * @see #getClient
     */
    @lombok.Getter
    private Map<Class, Object> clients = null;

    /**
     * Session Pool.
     */
    @lombok.Getter
    private ObjectPool<Session> sessionPool = null;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 记录异步回调任务（id:task 映射）。
     */
    private final Map<Long, FutureTaskDelegate<?>> callbackTasks = new ConcurrentHashMap<>(128);

    /**
     * 回调任务延迟清理队列。
     */
    private final DelayQueue<CallBackTask<Long>> evictCallBackTaskQueue = new DelayQueue();

    /**
     * 清理超时任务的线程。
     */
    private final CleanerThread cleanerThread = new CleanerThread();

    /**
     * 10s timeout to clean expired task.
     */
    public static final long DEFAULT_TIMEOUT_MILLISECONDS = 10000;

    /**
     * 清理回调任务记录的默认等待时间（超过此时间将清理回调记录，清理后即使有回调返回值也无法调用回调方法）。
     * 推荐任何异步回调等待都必需设置超时时间，避免无限等待。
     */
    @lombok.Getter
    private long defaultCallbackTimeoutMilliseconds = DEFAULT_TIMEOUT_MILLISECONDS;

    /**
     * 返回空值的{@code Callable}.
     */
    private static final Callable<Void> NULL_CALLABLE = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            return null;
        }
    };

    /**
     * 基于javassist的对象生成工厂.
     */
    @lombok.Getter
    private ObjectFactory clientObjectFactory = null;

    /**
     * Constructor.
     *
     * @param pool Session pool.
     * @param properties Properties.
     */
    public WebSocketClientActionFactory(ObjectPool<Session> pool, Map<String, Object> properties) {
        super(properties);
        this.sessionPool = pool;
        //对象序列/反序列化工具
        ObjectSerialization objectSerialization = Constants.DEFAULT_OBJECT_SERIALIZATION;
        Object val = null;
        //set objectSerialization
        if ((val = properties.get("objectSerialization")) != null) {
            objectSerialization = loadComponent(ObjectSerialization.class, val);
            log.info("Set objectSerialization : " + objectSerialization);
        }

        //TODO
        Map<Byte, ObjectSerialization> supportedObjectSerializations = Constants.SUPPORTED_OBJECT_SERIALIZATIONS;
        //set supportedObjectSerializations
        val = properties.get("supportedObjectSerializations");
        if (val != null) {
            supportedObjectSerializations = loadComponent(Map.class, val);
            log.info("Set supportedObjectSerializations : " + supportedObjectSerializations);
        }
        //内部实现
        protocolSerialization = new ProtocolSerialization(objectSerialization, supportedObjectSerializations);
        log.info("Use protocolSerialization : " + protocolSerialization);

        //set clientObjectFactory
        if ((val = properties.get("clientObjectFactory")) != null) {
            clientObjectFactory = loadComponent(ObjectFactory.class, val);
            log.info("Set clientObjectFactory : " + clientObjectFactory);
        }
        if (clientObjectFactory == null) {
            clientObjectFactory = createDefaultClientObjectFactory();
            log.info("No clientObjectFactory setting, use default : {}", clientObjectFactory);
        }

        clients = new HashMap<>();
        cleanerThread.setName(this.getClass().getName() + "-" + cleanerThread.getClass().getSimpleName());
        cleanerThread.setDaemon(false);
        cleanerThread.start();
    }

    /**
     * 未设置clientObjectFactory属性时，提供默认的{@code ObjectFactory}实现。
     * 默认提供{@link JavassistObjectFactory}。
     *
     * @return {@code ObjectFactory}对象。
     */
    private ObjectFactory createDefaultClientObjectFactory() {
        return new JavassistObjectFactory(this) {
            @Override
            public String parsePath(Class<?> targetClass, Method method) {
                return WebSocketClientActionFactory.this.parsePath(targetClass, method);
            }
        };
    }

    @Override
    public <T> T getClient(Class<T> clientClass) {
        return (T) clients.get(clientClass);
    }

    /**
     * 通过指定{@code String}类型路径和参数，转换为{@link Protocol}协议对象发送 WebSocket {@code byte[]}类型消息。
     *
     * @param path 指定的路径。
     * @param params 调用参数。
     *
     * @return 调用结果。
     *
     * @throws RpcException 如果发生调用错误。
     */
    @Override
    public Object invokeAction(String path, Object... params) throws RpcException {
        Session session = null;
        try {
            session = sessionPool.borrowObject();
        } catch (Exception ex) {
            throw new RpcException("Unable to borrow Session from pool.", ex);
        }
        if (session == null || !session.isOpen()) {
            log.error("Can't get WebSocket session or session is not open.");
            return null;
        }
        try {
            //实际参数
            List<Object> actualParams = null;
            //callback
            ResultCallback callback = null;
            //TODO
            //customized SendHandler
            SendHandler customizedSendHandler = null;
            //rebuild parameters
            if (CollectionUtil.isNotEmpty(params)) {
                actualParams = new ArrayList<>(params.length);
                for (Object param : params) {
                    if (param instanceof ResultCallback) {
                        callback = (ResultCallback) param;
                        continue;
                    }
                    if (param instanceof SendHandler) {
                        customizedSendHandler = (SendHandler) param;
                        continue;
                    }
                    actualParams.add(param);
                }
            }

            final RpcProtocol protocol = new RpcProtocol();
            final Long id = idGenerator.generateId();
            protocol.setId(id);
            protocol.setPath(path);
            if (CollectionUtil.isNotEmpty(actualParams)) {
                protocol.setParameters(actualParams.toArray());
            }

            //其次判断当前线程副本存在
            if (callback == null) {
                ResultCallback cb = RpcClientContext.get().getResultCallback();
                if (cb != null) {
                    callback = cb;
                }
            }

            if (callback != null) {
                final FutureTaskDelegate<?> callBackTaskDelegate = new FutureTaskDelegate(id, NULL_CALLABLE);
                //register task first
                callbackTasks.put(id, callBackTaskDelegate);
                SendHandler sendHandler = new SendHandler() {
                    @Override
                    public void onResult(SendResult result) {
                        if (result.isOK()) {
                            if (log.isDebugEnabled()) {
                                log.debug("Send WebSocket binary OK for [{}].", protocol);
                            }
                            //TODO customized callback timeout
                            evictCallBackTaskQueue.offer(new CallBackTask(System.currentTimeMillis() + defaultCallbackTimeoutMilliseconds, id));
                        } else {
                            callbackTasks.remove(id);
                            log.error("Exception occured when using WebSocket session to sent binary for [{}].", protocol);
                        }
                    }
                };
                session.getAsyncRemote().sendBinary(getObjectSerialization().serializeByteBuffer(protocol), sendHandler);
                try {
                    //TODO callback blocks invoker thread
                    callback.callback(callBackTaskDelegate);
                } finally {
                    callbackTasks.remove(id);
                }
            } else {
                session.getAsyncRemote().sendBinary(getObjectSerialization().serializeByteBuffer(protocol));
            }
            return null;
        } finally {
            RpcClientContext.remove();
            try {
                sessionPool.returnObject(session);
            } catch (Exception ex) {
                log.error("Exception occured when returning session to the pool.", ex);
                //ignore
            }
        }
    }

    /**
     * 接收 WebSocket {@code byte[]}类型消息，转换至{@link Protocol}协议对象，
     * 进而根据协议Id进行相应的回调处理。
     *
     * @param <T> type.
     * @param messages bytes data.
     * @param session WebSocket Session object.
     *
     * @return 调用结果。
     *
     * @throws RpcException 如果发生调用错误。
     */
    @Override
    public <T> T onMessage(byte[] messages, Session session) throws RpcException {
        Protocol<String> protocol = parseRpcProtocol(messages, session);
        if (protocol != null) {
            if (log.isDebugEnabled()) {
                log.debug("Received feedback protocol [{}].", protocol);
            }
            return onProtocol(protocol);
        }
        return null;
    }

    /**
     * 子类继承，可实现不同{@code Protocol}在客户端的调用；默认回调等待的任务。
     *
     * @param <T> type.
     * @param protocol {@code Protocol}.
     *
     * @return 调用结果。
     */
    protected <T> T onProtocol(Protocol<String> protocol) {
        Long id = protocol.getId();
        FutureTaskDelegate task = callbackTasks.remove(id);
        if (task != null) {
            Object res = null;
            task.set(res = protocol.getResult());
            //use thread pool run task???
            task.run();
            //TODO
            //evictCallBackTaskQueue.remove(id);
            return (T) res;
        }
        return null;
    }

    @Override
    public void clear() {
        super.clear();
        clients.clear();
        if (cleanerThread != null) {
            log.info("Shutdown thread [{}].", cleanerThread.getName());
            cleanerThread.shutdown();
            try {
                cleanerThread.join();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                //ignore
            }
        }
        callbackTasks.clear();
        evictCallBackTaskQueue.clear();
        if (sessionPool != null) {
            sessionPool.close();
        }
    }

    /**
     * 清理回调任务记录的默认等待时间；此值必需大于0.
     *
     * @param defaultCallbackTimeoutMilliseconds 清理回调任务记录的默认等待时间。
     */
    public void setDefaultCallbackTimeoutMilliseconds(long defaultCallbackTimeoutMilliseconds) {
        if (defaultCallbackTimeoutMilliseconds <= 0) {
            throw new IllegalArgumentException("Value can't be zero or negative.");
        }
        this.defaultCallbackTimeoutMilliseconds = defaultCallbackTimeoutMilliseconds;
    }

    @Override
    public ProtocolSerialization getObjectSerialization() {
        return this.protocolSerialization;
    }

    /**
     * 包含指定{@code Long}型Id的{@code FutureTask}.
     *
     * @param <V> Value data.
     */
    private final class FutureTaskDelegate<V> extends FutureTask<V> {

        /**
         * Id.
         */
        private final Long id;

        /**
         * Constructor.
         */
        public FutureTaskDelegate(Long id, Callable<V> callable) {
            super(callable);
            this.id = id;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            throw new UnsupportedOperationException("Please use method get(long timeout, TimeUnit unit).");
        }

        @Override
        protected void set(V v) {
            super.set(v);
        }

        @Override
        protected void done() {
            callbackTasks.remove(id);
        }

    }

    /**
     * 给定延迟时间之后执行的回调对象。
     *
     * @param <T> data type.
     */
    private static final class CallBackTask<T> implements Delayed {

        /** The time the task is enabled to execute in milliseconds Time units */
        private volatile long time;

        /** Data object */
        private final T data;

        /**
         * Constructor.
         */
        public CallBackTask(long time, T data) {
            this.time = time;
            this.data = data;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(time - now(), TimeUnit.MILLISECONDS);
        }

        //time now
        private long now() {
            return System.currentTimeMillis();
        }

        @Override
        public int compareTo(Delayed other) {
            // compare zero ONLY if same object
            if (other == this) {
                return 0;
            }
            if (other instanceof CallBackTask) {
                CallBackTask x = (CallBackTask) other;
                long diff = time - x.time;
                return (diff == 0) ? 0 : ((diff < 0) ? -1 : 1);
            }
            long d = (getDelay(TimeUnit.NANOSECONDS)
                    - other.getDelay(TimeUnit.NANOSECONDS));
            return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
        }

    }

    /**
     * Cleaner Thread.
     */
    private class CleanerThread extends Thread {

        //running flag
        private volatile boolean running = true;

        @Override
        public void run() {
            log.info("Start thread [{}].", this.getName());
            while (running) {
                try {
                    CallBackTask<Long> task = evictCallBackTaskQueue.take();
                    if (task.data != null) {
                        if (log.isInfoEnabled()) {
                            log.info("Clean expired Task: [{}] ", task.data);
                        }
                        callbackTasks.remove(task.data);
                    }
                } catch (Throwable ex) {
                    log.error("Throwable occured.", ex);
                }
            }
            log.info("Finish thread [{}].", this.getName());
        }

        /**
         * Stop thread task.
         */
        public void shutdown() {
            running = false;
            //offer an immediate end task if queue is empty
            evictCallBackTaskQueue.offer(new CallBackTask(System.currentTimeMillis(), null));
        }
    }

    /**
     * 根据{@code Session}消息解析{@code Protocol}对象。
     *
     * @param messages byte[]类型消息。
     * @param session {@code Session}对象。
     *
     * @return {@code Protocol}对象。
     */
    private Protocol<String> parseRpcProtocol(byte[] messages, Session session) {
        try {
            return getObjectSerialization().deserialize(messages, RpcProtocol.class);
        } catch (Throwable t) {
            log.error("Exception ocurred when parsing Rpc Protocol.", t);
            Protocol<String> protocol = new RpcProtocol();
            protocol = getObjectSerialization().fillProtocol(messages, protocol);
            return protocol;
        }
    }

    @Override
    public void addActions(Object obj) {
        Class<?> clientInterface = null;
        //clinet only accpets interface
        if (obj instanceof Class && ((clientInterface = (Class) obj)).isInterface()) {
            if (clientAnnotatedClass != null && !clientInterface.isAnnotationPresent(clientAnnotatedClass)) {
                throw new JRouterException("Client interface [" + clientInterface + "] should has @" + clientAnnotatedClass + ".");
            }
            //因为需代理方法参数，方法名称需唯一
            //create dynamic interface
            Object exist = clients.put(clientInterface, clientObjectFactory.newInstance(clientInterface));
            if (exist != null) {
                throw new JRouterException("Client [" + clientInterface + "] already exists.");
            }
        } else {
            log.warn("Only accepts interface types, ignore [{}]", obj == null ? "null" : obj.getClass());
        }
    }

    /**
     * 由指定的方法生成{@code String}型路径。
     *
     * @param targetClass 底层方法所表示的 {@code Class} 对象。
     * @param method 指定的底层方法。
     *
     * @return 用于{@link #invokeAction}的{@code String}型调用路径。
     *
     * @see #clientObjectFactory
     */
    protected String parsePath(Class<?> targetClass, Method method) {
        Namespace ns = getNamespace(targetClass);
        //trim empty and '/'
        String namespace = ns == null ? Character.toString(getPathSeparator()) : getPathSeparator() + StringUtil.trim(ns.name(), getPathSeparator());
        return buildActionPath(namespace, "", method);
    }

    /**
     * 提供{@link RpcConsumer}至{@link Namespace}的转换。
     *
     * @param clazz 指定的类型。
     *
     * @return {@code Namespace}对象。
     *
     * @deprecated
     */
    @Override
    protected Namespace getNamespace(Class<?> clazz) {
        final RpcConsumer consumer = clazz.getAnnotation(RpcConsumer.class);
        if (consumer != null) {
            String namespace = consumer.namespace();
            if (StringUtil.isBlank(namespace)) {
                namespace = clazz.getCanonicalName();
            }
            final String name = namespace;
            return new Namespace() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public String interceptorStack() {
                    return consumer.interceptorStack();
                }

                @Override
                public String[] interceptors() {
                    return consumer.interceptors();
                }

                @Override
                public boolean autoIncluded() {
                    return consumer.autoIncluded();
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return RpcConsumer.class;
                }
            };
        }
        return super.getNamespace(clazz);
    }
}
