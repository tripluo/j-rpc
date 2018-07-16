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

import net.jrouter.rpc.DemoClientInterface;
import net.jrouter.rpc.DemoClientInterface2;
import net.jrouter.rpc.TomcatBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import static org.testng.Assert.*;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * SpringWebSocketClientActionFactoryNGTest.
 *
 * @see net.jrouter.rpc.spring.websocket.config.SpringWebSocketClientActionFactoryConfiguration
 */
@ContextConfiguration(locations = {TomcatBaseTest.SPRING_CONFIG_LOCATION_PARAM})
@Slf4j
public final class SpringWebSocketClientActionFactoryNGTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private SpringWebSocketClientActionFactory springWebSocketClientActionFactory;

    @Autowired
    private DemoClientInterface2 demoClientInterface2;

    @BeforeClass
    public void setUpMethod() throws Exception {
        assertNotNull(springWebSocketClientActionFactory);
        assertNotNull(demoClientInterface2);
        assertSame(demoClientInterface2, applicationContext.getBean(DemoClientInterface2.class));
    }

    @AfterGroups
    public void tearDownMethod() throws Exception {
        springWebSocketClientActionFactory.clear();
    }

    /**
     * Test of addActions method, of class SpringWebSocketClientActionFactory.
     */
    @Test
    public void testAddActions() {
        springWebSocketClientActionFactory.addActions(DemoClientInterface.class);
        DemoClientInterface client = springWebSocketClientActionFactory.getClient(DemoClientInterface.class);
        assertSame(client, applicationContext.getBean(DemoClientInterface.class));
        assertNotSame(client, demoClientInterface2);
    }

}
