package com.docilewolf.baseProxy;

import com.docilewolf.annotation.NotProxy;
import com.docilewolf.chain.ProxyChain;

import java.lang.reflect.Method;

/**
 * Created by stefan on 16-2-15.
 */
public class AbstractProxy implements Proxy {
    public final void doProxy(ProxyChain proxyChain) {
        Class<?> targetClass = proxyChain.getTargetClass();
        Object targetObject = proxyChain.getTargetObject();
        Method targetMethod = proxyChain.getMethod();
        Object[] params = proxyChain.getParams();
        if(isProxy(targetClass, targetObject, targetMethod, params)){
            //此处用到钩子方法,子类具体实现自己的切面方法
            doBefore(proxyChain);
            proxyChain.doChain();
            doAfter(proxyChain);
        }else{
            proxyChain.doChain();
        }
    }

    //是否需要拦截,默认实现,可重写
    public boolean isProxy(Class<?> targetClass, Object targetObject, Method method, Object[] args){
        if(null == method.getAnnotation(NotProxy.class)){
            return true;
        }
        return false;
    }

    public void doBefore(ProxyChain proxyChain){}

    public void doAfter(ProxyChain proxyChain){}
}
