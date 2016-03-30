package com.docilewolf;

import com.docilewolf.baseProxy.Proxy;
import com.docilewolf.cglib.ProxyHandler;
import com.docilewolf.aopProxy.AfterProxy;
import com.docilewolf.aopProxy.BeforeProxy;
import com.docilewolf.aopProxy.OtherAfterProxy;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stefan on 16-2-15.
 */
public class Main {
    public static void main(String[] args){
        List<Proxy> list = new LinkedList<Proxy>();
        list.add(new AfterProxy());
        list.add(new OtherAfterProxy());
        list.add(new BeforeProxy());

        ProxyHandler proxyHandler = new ProxyHandler();

        TargetObject targetObject = (TargetObject) proxyHandler.getProxy(TargetObject.class, list);

        targetObject.proxyMethod();
        targetObject.notProxyMethod();
    }
}
