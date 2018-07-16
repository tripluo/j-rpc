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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.jrouter.rpc.RpcException;

/**
 * FutureUtil.
 */
public class FutureUtil {

    /**
     * Default wait 10 seconds.
     */
    public static final int DEFAULT_WAIT_SECONDS = 10;

    /**
     * @see #handle(java.util.concurrent.Future, java.lang.Class)
     */
    public static <T> T handle(Future<T> result) {
        return handle(result, null);
    }

    /**
     * Handle getting result from {@code Future}, convert any exception to {@code RpcException} and throw it.
     *
     * @param <T> Future result type.
     * @param result Future object.
     * @param expectedClass Expected result class type.
     *
     * @return Future result.
     */
    public static <T> T handle(Future<T> result, Class<?> expectedClass) {
        try {
            Object res = result.get(DEFAULT_WAIT_SECONDS, TimeUnit.SECONDS);
            if (res != null) {
                if (expectedClass != null && expectedClass.isInstance(res)) {
                    return (T) res;
                }
                //TODO
                if (res instanceof RpcException) {
                    throw (RpcException) res;
                }
                if (res instanceof Throwable) {
                    throw new RpcException((Throwable) res);
                }
            }
            return (T) res;
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            throw new RpcException(ex);
        }
    }
}
