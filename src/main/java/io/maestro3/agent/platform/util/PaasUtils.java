package io.maestro3.agent.platform.util;

public final class PaasUtils {

    private static final String FILE_STORAGE_PAAS_DIR = "SYSTEM/PaaS/%s";

    private PaasUtils() {
        throw new UnsupportedOperationException("Class is not designed for an instantiation");
    }

    public static String getFileStoragePaasDirectory(final String serviceName) {
        return String.format(FILE_STORAGE_PAAS_DIR, serviceName);
    }
}
