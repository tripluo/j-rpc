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
package net.jrouter.rpc;

import java.util.List;
import net.jrouter.rpc.annotation.RpcConsumer;
import net.jrouter.rpc.protocol.Protocol;
import net.jrouter.rpc.router.ResultCallback;

/**
 * DemoClientInterface.
 *
 * @see DemoService
 */
@RpcConsumer(namespace = "net.jrouter.rpc.DemoService")
public interface DemoClientInterface {

    void no_method();

    /**
     * Simple return current time.
     */
    long time();

    /**
     * Echo {@code String}.
     */
    String echo(String str);

    String echo(String str, ResultCallback callback);

    boolean checkProtocol(List<Protocol> protocols);

    Protocol<String> getProtocol(int number, String str, List<String> list, ResultCallback callback);

    String exception(String test, ResultCallback callback);

    void sleep(int millis, ResultCallback callback);
}
