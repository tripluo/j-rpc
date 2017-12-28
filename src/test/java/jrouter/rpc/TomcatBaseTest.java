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

import javax.servlet.ServletContext;
import javax.websocket.server.ServerContainer;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ApplicationParameter;
import org.apache.tomcat.websocket.server.Constants;
import org.apache.tomcat.websocket.server.WsContextListener;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Tomcat Base Test.
 */
public abstract class TomcatBaseTest {

    protected static Tomcat TOMCAT;

    private static final int PORT = 9999;

    public static final String SPRING_CONFIG_LOCATION_PARAM = "classpath:jrouter-rpc-spring_test.xml";

    public static final String CONTEXT_PATH = "/jrouter-rpc";

    protected static ServletContext SERVLET_CONTEXT;

    protected static ServerContainer SERVER_CONTAINER;

    //WebApplicationContext
    protected static WebApplicationContext WEB_APPLICATION_CONTEXT;

    /**
     * Sub-classes need to know port so they can connect
     */
    public static int getPort() {
        return TOMCAT == null ? PORT : TOMCAT.getConnector().getLocalPort();
    }

    @BeforeSuite
    public static final void setUpTomcat() throws Exception {
        TOMCAT = new Tomcat();
        TOMCAT.setPort(PORT);
        Context ctx = TOMCAT.addContext(CONTEXT_PATH, null);
        Tomcat.addServlet(ctx, "default", new DefaultServlet());
        ctx.addServletMappingDecoded("/", "default");

        ctx.addApplicationListener(WsContextListener.class.getName());

        ApplicationParameter ap = new ApplicationParameter();
        ap.setName(ContextLoader.CONFIG_LOCATION_PARAM);
        ap.setValue(SPRING_CONFIG_LOCATION_PARAM);
        ctx.addApplicationParameter(ap);
        ctx.addApplicationListener(ContextLoaderListener.class.getName());

        TOMCAT.start();

        SERVLET_CONTEXT = ctx.getServletContext();
        //start then get ServerContainer
        SERVER_CONTAINER = (ServerContainer) SERVLET_CONTEXT.getAttribute(Constants.SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE);
        WEB_APPLICATION_CONTEXT = WebApplicationContextUtils.getRequiredWebApplicationContext(SERVLET_CONTEXT);
    }

    @AfterSuite
    public static final void tearDownTomcat() throws Exception {
        // Some tests may call tomcat.destroy(), some tests may just call
        // tomcat.stop(), some not call either method. Make sure that stop()
        // & destroy() are called as necessary.
        if (TOMCAT.getServer() != null
                && TOMCAT.getServer().getState() != LifecycleState.DESTROYED) {
            if (TOMCAT.getServer().getState() != LifecycleState.STOPPED) {
                TOMCAT.stop();
            }
            TOMCAT.destroy();
        }
    }
}
