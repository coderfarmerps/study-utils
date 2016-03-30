package com.docilewolf.cglib;

import com.docilewolf.baseProxy.Proxy;
import com.docilewolf.chain.ProxyChain;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by stefan on 16-2-15.
 * 代理类
 */
public class ProxyHandler implements MethodInterceptor {

    private List<Proxy> proxyList;
    private Class<?> targetClass;

    public Object getProxy(Class clazz, List<Proxy> proxyList){
        this.targetClass = clazz;
        this.proxyList = proxyList;
        return Enhancer.create(clazz, this);
    }

    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        ProxyChain proxyChain = new ProxyChain(proxyList, this.targetClass, o, method, methodProxy, objects);
        return proxyChain.doChain();
    }
}
