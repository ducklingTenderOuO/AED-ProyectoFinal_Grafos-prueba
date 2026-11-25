package io;

import model.Arista;
import model.Grafo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GestorCSV {

    public static ResultadoImportacion importarCSV(File archivo, Grafo grafo) {
        ResultadoImportacion resultado = new ResultadoImportacion();
        if (archivo == null || !archivo.exists() || !archivo.canRead()) {
            resultado.agregarError("El archivo no existe o no tiene permisos de lectura.");
            return resultado;
        }

        grafo.limpiar();
        Set<String> conexionesProcesadas = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
            String encabezado = br.readLine();
            resultado.setTotalLineas(1);

            if (encabezado == null || encabezado.trim().isEmpty()) {
                resultado.agregarError("El archivo CSV está completamente vacío.");
                return resultado;
            }

            String[] columnas = encabezado.split(",");
            if (columnas.length < 3) {
                resultado.agregarError("Encabezado inválido. Se requieren al menos 3 columnas: origen,destino,peso");
                return resultado;
            }

            String c0 = columnas[0].trim().toLowerCase();
            String c1 = columnas[1].trim().toLowerCase();
            String c2 = columnas[2].trim().toLowerCase();

            if (!c0.equals("origen") || !c1.equals("destino") || !c2.equals("peso")) {
                resultado.agregarAdvertencia("Encabezado no estándar ('" + encabezado + "'). Se asumirá columna 0=origen, 1=destino, 2=peso.");
            }

            String linea;
            int numLinea = 1;
            while ((linea = br.readLine()) != null) {
                numLinea++;
                resultado.setTotalLineas(numLinea);
                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(",");

                if (partes.length < 3) {
                    resultado.agregarAdvertencia("Línea " + numLinea + ": Registro incompleto (se esperan 3 campos): '" + linea + "'");
                    continue;
                }

                String strOrigen = partes[0].trim();
                String strDestino = partes[1].trim();
                String strPeso = partes[2].trim();

                if (strOrigen.isEmpty() || strDestino.isEmpty()) {
                    resultado.agregarAdvertencia("Línea " + numLinea + ": Vértice de origen o destino vacío.");
                    continue;
                }

                int origen;
                int destino;
                double peso;

                try {
                    origen = Integer.parseInt(strOrigen);
                    destino = Integer.parseInt(strDestino);
                } catch (NumberFormatException e) {
                    resultado.agregarAdvertencia("Línea " + numLinea + ": Vértices deben ser enteros ('" + strOrigen + "', '" + strDestino + "')");
                    continue;
                }

                if (origen == destino) {
                    resultado.agregarAdvertencia("Línea " + numLinea + ": Lazo omitido (" + origen + " -> " + destino + "). No se permiten conexiones consigo mismo.");
                    continue;
                }

                try {
                    peso = Double.parseDouble(strPeso);
                    if (peso < 0) {
                        resultado.agregarAdvertencia("Línea " + numLinea + ": Peso negativo descartado (" + peso + "). Debe ser >= 0.");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    resultado.agregarAdvertencia("Línea " + numLinea + ": Peso no numérico: '" + strPeso + "'");
                    continue;
                }

                String claveConexion = grafo.isDirigido()
                        ? (origen + "->" + destino)
                        : (Math.min(origen, destino) + "<->" + Math.max(origen, destino));

                if (conexionesProcesadas.contains(claveConexion)) {
                    resultado.agregarAdvertencia("Línea " + numLinea + ": Conexión duplicada entre " + origen + " y " + destino + ". Se actualiza el peso a " + peso);
                }
                conexionesProcesadas.add(claveConexion);

                grafo.agregarArista(origen, destino, peso);
            }

            resultado.setVerticesImportados(grafo.getOrden());
            resultado.setAristasImportadas(grafo.getTamaño());

        } catch (IOException e) {
            resultado.agregarError("Error de E/S al leer el archivo: " + e.getMessage());
        }

        return resultado;
    }

    public static boolean exportarCSV(File archivo, Grafo grafo) {
        if (archivo == null) return false;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {

            bw.write("origen,destino,peso");
            bw.newLine();

            List<Arista> aristas = grafo.getTodasLasAristas();
            for (Arista a : aristas) {
                bw.write(a.getOrigen() + "," + a.getDestino() + "," + a.getPeso());
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error al exportar CSV: " + e.getMessage());
            return false;
        }
    }
}
}
