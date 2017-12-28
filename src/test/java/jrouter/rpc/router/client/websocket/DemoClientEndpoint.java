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

import javax.websocket.ClientEndpoint;
import javax.websocket.Session;
import jrouter.rpc.router.RpcActionFactory;
import jrouter.rpc.router.server.websocket.WebSocketServerActionFactory;
import jrouter.rpc.transport.http.websocket.WebSocketEndpoint;
import lombok.extern.slf4j.Slf4j;

/**
 * Demo websocket Client Endpoint.
 */
@ClientEndpoint
@Slf4j
public class DemoClientEndpoint extends WebSocketEndpoint {
}
