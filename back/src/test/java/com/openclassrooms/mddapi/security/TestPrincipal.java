package com.openclassrooms.mddapi.security;

import lombok.Getter;

@Getter
public class TestPrincipal {

    private final Long id;

    public TestPrincipal(Long id) {
        this.id = id;
    }

}
