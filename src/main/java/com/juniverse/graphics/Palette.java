
package com.juniverse.graphics;

/**
 * Color de la paleta en componentes RGB.
 *
 * @param r rojo (0-255)
 * @param g verde (0-255)
 * @param b azul (0-255)
 * @author linux
 */
public record Palette(int r, int g, int b) {

    /** Representación en texto del color, útil para depurar. */
    @Override
    public String toString() {
        return "Palette{" + "r=" + r + ", g=" + g + ", b=" + b + '}';
    }

}
