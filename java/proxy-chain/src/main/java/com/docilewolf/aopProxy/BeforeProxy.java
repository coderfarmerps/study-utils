package com.docilewolf.aopProxy;

import com.docilewolf.baseProxy.AbstractProxy;
import com.docilewolf.chain.ProxyChain;

/**
 * Created by stefan on 16-2-15.
 */
public class BeforeProxy extends AbstractProxy {

    @Override
    public void doBefore(ProxyChain proxyChain) {
        System.out.println(proxyChain.getMethod() + ": first do before");
    }
}
