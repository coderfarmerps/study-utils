package com.docilewolf.aopProxy;

import com.docilewolf.baseProxy.AbstractProxy;
import com.docilewolf.chain.ProxyChain;

/**
 * Created by stefan on 16-2-15.
 */
public class OtherAfterProxy extends AbstractProxy {
    @Override
    public void doAfter(ProxyChain proxyChain) {
        System.out.println(proxyChain.getMethod() + ": other do after");
    }
}
