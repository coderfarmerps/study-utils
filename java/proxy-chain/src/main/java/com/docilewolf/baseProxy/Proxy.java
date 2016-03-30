package com.docilewolf.baseProxy;

import com.docilewolf.chain.ProxyChain;

/**
 * Created by stefan on 16-2-15.
 */
public interface Proxy {
    public void doProxy(ProxyChain proxyChain);
}
