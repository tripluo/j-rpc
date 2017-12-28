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
package jrouter.rpc;

import java.util.List;
import javax.websocket.Session;
import jrouter.ActionInvocation;
import jrouter.rpc.annotation.RpcProvider;
import jrouter.rpc.protocol.Protocol;
import lombok.extern.slf4j.Slf4j;
import jrouter.rpc.router.RpcActionInvocation;

/**
 * Demo service.
 *
 * @see DemoClientInterface
 */
@RpcProvider
@Slf4j
public class DemoService {

    public long time() {
        return System.currentTimeMillis();
    }

    public String echo(String msg, ActionInvocation<String> invocation) {
        log.info(invocation.getActionPath());
        return msg;
    }

    public Protocol<String> getProtocol(int number, String str, List<String> list,
            RpcActionInvocation<Session> invocation) {
        return invocation.getProtocol();
    }

    public String exception(String msg) {
        throw new IllegalArgumentException(msg);
    }

    public void sleep(int millis) throws InterruptedException {
        Thread.sleep(millis);
    }

}
