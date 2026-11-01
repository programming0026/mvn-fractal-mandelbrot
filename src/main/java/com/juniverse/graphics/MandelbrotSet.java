package com.juniverse.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.commons.numbers.complex.Complex;

/**
 * Dibuja el conjunto de Mandelbrot (z = z² + c, con c igual al punto del plano) en un panel Swing,
 * coloreando cada punto según el número de iteraciones que tarda en divergir.
 *
 * @author linux
 */
public class MandelbrotSet extends JPanel {

    private static final double REALMIN = -2.0;
    private static final double IMAGMIN = -1.5;
    private static final double REALMAX = 1.0;
    private static final double IMAGMAX = 1.5;
    private static final double DIVERGE = 4.0;
    private static final int MAX = 256;

    private int maxx;
    private int maxy;
    private final Palette[] palette;

    /** Crea el panel y carga la paleta de 256 colores desde palette.pal. */
    public MandelbrotSet() {
        this.palette = load("palette.pal");
        this.maxx = getWidth();
        this.maxy = getHeight();
    }

    /** Limpia el panel y vuelve a dibujar el fractal con el tamaño actual. */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        clearDevice(g2d);
        draw(g2d);
    }

    /**
     * Carga la paleta de colores (una línea "R G B" por color) desde el classpath.
     * @param fileName nombre del recurso dentro de resources
     * @return paleta de {@code MAX} colores; las entradas ausentes quedan en negro
     */
    private Palette[] load(String fileName) {
        Palette[] color = new Palette[MAX];
        // Se lee con getResourceAsStream para que funcione también dentro del JAR empaquetado
        // (Paths.get(url.toURI()) falla cuando el recurso está dentro de un archivo .jar).
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new IOException("No se encontró el recurso " + fileName);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            int i = 0;
            String line;
            while ((line = reader.readLine()) != null && i < MAX) {
                if (line.isBlank()) {
                    continue;
                }
                Scanner sc = new Scanner(line);
                color[i++] = new Palette(sc.nextInt(), sc.nextInt(), sc.nextInt());
            }
        } catch (IOException ex) {
            Logger.getLogger(MandelbrotSet.class.getName()).log(Level.SEVERE, "No se pudo cargar la paleta", ex);
        }
        // Completa con negro las posiciones que falten para evitar NullPointerException al pintar.
        for (int i = 0; i < MAX; i++) {
            if (color[i] == null) {
                color[i] = new Palette(0, 0, 0);
            }
        }
        return color;
    }

    /**
     * Devuelve el color de la paleta para un número de iteraciones.
     * @param index posición en la paleta (0-255)
     */
    private Palette getColor(int index) {
        return palette[index];
    }

    /**
     * Pinta un píxel del panel.
     * @param g2d contexto gráfico
     * @param x columna
     * @param y fila
     * @param color color del píxel
     */
    private void setPixel(Graphics2D g2d, int x, int y, Color color) {
        g2d.setColor(color);
        g2d.fill3DRect(x, y, 1, 1, true);
    }

    /** Rellena el panel de negro antes de dibujar. */
    private void clearDevice(Graphics2D g2d) {
        g2d.setBackground(Color.BLACK);
        g2d.clearRect(0, 0, this.maxx, this.maxy);
    }

    /**
     * Recorre cada píxel, lo convierte a un punto del plano complejo, itera la fórmula
     * hasta que diverge (módulo > 4) o llega a {@code MAX} y lo colorea según las iteraciones.
     * Los puntos que no divergen se pintan de negro.
     */
    private void draw(Graphics2D g2d) {
        this.maxx = getWidth();
        this.maxy = getHeight();
        Complex z;
        Complex c;
        int cont;

        for (int i = 0; i < maxx; ++i) {
            for (int j = 0; j < maxy; ++j) {
                //asignamos a C las coordenadas del plano complejo
                c = Complex.ofCartesian(((REALMAX - REALMIN) / (maxx)) * i + REALMIN, ((IMAGMAX - IMAGMIN) / (maxy)) * j + IMAGMIN);
                //iniciamos Z=0+0i
                z = Complex.ZERO;
                //inicializamos el contador
                cont = 0;

                do {
                    //Z = (Z * Z) + C --> ecuacion Z=Z²+C                    
                    z = z.multiply(z).add(c);
                    cont++;

                    if (z.norm() > DIVERGE) {
                        break;
                    }
                } while (cont < MAX);

                Palette palette = getColor(clamp(cont + 5));
                int r = palette.r();
                int g = palette.g();
                int b = palette.b();

                if (cont == MAX) {
                    setPixel(g2d, i, maxy - 1 - j, new Color(0, 0, 0));
                } else {
                    setPixel(g2d, i, maxy - 1 - j, new Color(r, g, b));
                }

            }
        }

        g2d.dispose();

    }

    /** Limita un valor al rango 0-255 para usarlo como índice de la paleta. */
    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /** Crea y muestra la ventana con el fractal. */
    public static void createWin() {
        JFrame frame = new JFrame("Mandelbrot Set");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(640, 480);
        frame.setResizable(true);
        frame.add(new MandelbrotSet());
        frame.setVisible(true);
        setLocationCenter(frame);
    }

    /** Centra la ventana en la pantalla. */
    private static void setLocationCenter(JFrame frame) {
        setLocationMove(frame, 0, 0);
    }

    /**
     * Coloca la ventana centrada en la pantalla y la desplaza el desplazamiento indicado.
     * @param frame ventana a mover
     * @param moveWidth desplazamiento horizontal en píxeles
     * @param moveHeight desplazamiento vertical en píxeles
     */
    private static void setLocationMove(JFrame frame, int moveWidth, int moveHeight) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        frameSize.width = frameSize.width > screenSize.width ? screenSize.width : frameSize.width;
        frameSize.height = frameSize.height > screenSize.height ? screenSize.height : frameSize.height;
        frame.setLocation((screenSize.width - frameSize.width) / 2 + moveWidth, (screenSize.height - frameSize.height) / 2 + moveHeight);
    }

}
