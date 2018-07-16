package net.jrouter.rpc.router.interceptor;

import jrouter.annotation.Interceptor;
import jrouter.impl.InvocationProxyException;
import lombok.extern.slf4j.Slf4j;
import net.jrouter.rpc.router.RpcActionInvocation;

/**
 * ExceptionInterceptor.
 */
@Slf4j
public class ExceptionInterceptor {

    public static final String EXCEPTION = "__exception";

    @Interceptor(name = EXCEPTION)
    public Object intercept(RpcActionInvocation invocation) {
        Object result = null;
        try {
            //invoke
            result = invocation.invoke();
        } catch (Exception e) {
            if (e instanceof InvocationProxyException) {
                e = (Exception) ((InvocationProxyException) e).getSource();
            }
            invocation.setInvokeResult(e);
            log.error(invocation.getProtocol() + " ---> " + e.getLocalizedMessage());
        }
        return result;
    }

}
