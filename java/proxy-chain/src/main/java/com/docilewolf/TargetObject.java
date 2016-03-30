package com.docilewolf;

import com.docilewolf.annotation.NotProxy;

/**
 * Created by stefan on 16-2-15.
 */
public class TargetObject {

    public void proxyMethod(){
        System.out.println("this method has been proxyed");
    }

    @NotProxy
    public void notProxyMethod(){
        System.out.println("this method has not been proxyed");
    }
}
