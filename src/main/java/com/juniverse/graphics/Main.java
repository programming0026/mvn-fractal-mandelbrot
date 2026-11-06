package com.juniverse.graphics;

import javax.swing.SwingUtilities;

/**
 *
 * @author Java Universe
 */
public class Main {

    /**
     * Punto de entrada: abre la ventana del fractal en el hilo de Swing.
     * @param args no se usan
     */
    public static void main(String[] arg) {
        SwingUtilities.invokeLater(() -> MandelbrotSet.createWin());
    }
}
