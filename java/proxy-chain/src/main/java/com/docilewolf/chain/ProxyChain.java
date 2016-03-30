package com.docilewolf.chain;

import com.docilewolf.baseProxy.Proxy;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by stefan on 16-2-15.
 */
public class ProxyChain {

    private List<Proxy> proxyList;

    /**
     * 此处代表代理链路中走到哪个位置了,类似链表的指针
     * 此变量很重要, 如果没有位置指针可能会导致重复走链路的问题
     */
    private Integer currentIndex;

    //需要代理的类
    private Class<?> targetClass;
    //代理的目标对象
    private Object targetObject;
    //拦截方法
    private Method method;
    //拦截方法代理对象
    private MethodProxy methodProxy;
    //方法参数
    private Object[] params;

    public ProxyChain(List<Proxy> proxyList, Class<?> targetClass, Object targetObject, Method method, MethodProxy methodProxy, Object[] params) {
        if(proxyList == null){
            this.proxyList = new LinkedList<Proxy>();
        }else {
            this.proxyList = proxyList;
        }
        this.targetClass = targetClass;
        this.targetObject = targetObject;
        this.method = method;
        this.methodProxy = methodProxy;
        this.params = params;
        this.currentIndex = 0;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public MethodProxy getMethodProxy() {
        return methodProxy;
    }

    public void setMethodProxy(MethodProxy methodProxy) {
        this.methodProxy = methodProxy;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }
    public List<Proxy> getProxyList() {
        return proxyList;
    }

    public void setProxyList(List<Proxy> proxyList) {
        this.proxyList = proxyList;
    }

    public Object doChain(){
        if(currentIndex < proxyList.size()){
            //走完代理链路
            proxyList.get(currentIndex++).doProxy(this);
        }else{
            try {
                //调用目标方法
                return methodProxy.invokeSuper(targetObject, params);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        return null;
    }
}
