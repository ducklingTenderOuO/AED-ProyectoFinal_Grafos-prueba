package io;

import java.util.ArrayList;
import java.util.List;

public class ResultadoImportacion {
    private int totalLineas;
    private int aristasImportadas;
    private int verticesImportados;
    private final List<String> errores;
    private final List<String> advertencias;

    public ResultadoImportacion() {
        this.totalLineas = 0;
        this.aristasImportadas = 0;
        this.verticesImportados = 0;
        this.errores = new ArrayList<>();
        this.advertencias = new ArrayList<>();
    }

    public int getTotalLineas() {
        return totalLineas;
    }

    public void setTotalLineas(int totalLineas) {
        this.totalLineas = totalLineas;
    }

    public int getAristasImportadas() {
        return aristasImportadas;
    }

    public void setAristasImportadas(int aristasImportadas) {
        this.aristasImportadas = aristasImportadas;
    }

    public int getVerticesImportados() {
        return verticesImportados;
    }

    public void setVerticesImportados(int verticesImportados) {
        this.verticesImportados = verticesImportados;
    }

    public List<String> getErrores() {
        return errores;
    }

    public List<String> getAdvertencias() {
        return advertencias;
    }

    public void agregarError(String error) {
        errores.add(error);
    }

    public void agregarAdvertencia(String adv) {
        advertencias.add(adv);
    }

    public boolean esExitoso() {
        return errores.isEmpty();
    }

    public String generarReporte() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- REPORTE DE IMPORTACIÓN CSV ---\n");
        sb.append("Líneas procesadas: ").append(totalLineas).append("\n");
        sb.append("Vértices creados: ").append(verticesImportados).append("\n");
        sb.append("Aristas agregadas: ").append(aristasImportadas).append("\n");
        if (!advertencias.isEmpty()) {
            sb.append("\nAdvertencias (").append(advertencias.size()).append("):\n");
            for (String adv : advertencias) {
                sb.append("  [!] ").append(adv).append("\n");
            }
        }
        if (!errores.isEmpty()) {
            sb.append("\nErrores críticos (").append(errores.size()).append("):\n");
            for (String err : errores) {
                sb.append("  [X] ").append(err).append("\n");
            }
        }
        return sb.toString();
    }
}
