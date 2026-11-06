package com.nexus.backend.importer.provider;

import java.io.IOException;

/** Acceso a los fixtures de feeds desde otros paquetes de test. */
public final class ProvidersTestFixtures {

    private ProvidersTestFixtures() {
    }

    public static byte[] bytes(String name) throws IOException {
        return ProvidersTest.fixture(name);
    }
}
