package com.keepersecurity.spring.ksm.autoconfig;

import java.security.Provider;

public class TestBcProvider extends Provider {
    public TestBcProvider() {
        super("BC", 1.0, "test bc provider");
    }
}
