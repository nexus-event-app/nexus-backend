package com.nexus.backend.enums;

/**
 * Tipos de fuente de importación. Cada tipo sabe qué contenido produce y cada cuánto se le puede consultar
 * como mínimo (algunos proveedores lo exigen en sus condiciones de uso).
 */
public enum ImportSourceType {

    /** Cualquier feed RSS o Atom de noticias. */
    RSS_NEWS(true, 30, 1),
    /** API pública de Remotive: máximo 4 consultas al día según sus condiciones. */
    REMOTIVE_JOBS(false, 360, 360),
    /** API pública de Arbeitnow. */
    ARBEITNOW_JOBS(false, 60, 5),
    /** API pública de Jobicy: empleo en remoto; sus condiciones piden no consultarla más de una vez por hora. */
    JOBICY_JOBS(false, 360, 60),
    /** API pública de Get on Board: empleo tech en español con nivel explícito. */
    GETONBRD_JOBS(false, 120, 5);

    private final boolean news;
    private final int minIntervalMinutes;
    private final int manualCooldownMinutes;

    ImportSourceType(boolean news, int minIntervalMinutes, int manualCooldownMinutes) {
        this.news = news;
        this.minIntervalMinutes = minIntervalMinutes;
        this.manualCooldownMinutes = manualCooldownMinutes;
    }

    public boolean isNews() {
        return news;
    }

    /** Intervalo mínimo entre ejecuciones programadas. */
    public int getMinIntervalMinutes() {
        return minIntervalMinutes;
    }

    /** Tiempo mínimo desde la última ejecución para lanzar una manual. */
    public int getManualCooldownMinutes() {
        return manualCooldownMinutes;
    }
}
