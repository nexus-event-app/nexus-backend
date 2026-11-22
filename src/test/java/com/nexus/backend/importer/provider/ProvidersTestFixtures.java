package com.nexus.backend.importer.provider;

import java.io.IOException;

public final class ProvidersTestFixtures {

    private ProvidersTestFixtures() {
    }

    public static byte[] bytes(String name) throws IOException {
        return ProvidersTest.fixture(name);
    }
}
