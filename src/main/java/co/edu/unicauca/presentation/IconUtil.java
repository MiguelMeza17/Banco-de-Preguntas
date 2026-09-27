package co.edu.unicauca.presentation;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Carga los íconos de {@code /icons/*.png} (classpath) y los escala al tamaño
 * pedido. Los íconos se cachean por "nombre@tamaño" para no releer el archivo
 * cada vez que se abre una ventana.
 */
public final class IconUtil {

    private static final Map<String, ImageIcon> CACHE = new HashMap<>();

    private IconUtil() {
    }

    public static ImageIcon icon(String name, int size) {
        String key = name + "@" + size;
        return CACHE.computeIfAbsent(key, k -> loadScaled(name, size));
    }

    private static ImageIcon loadScaled(String name, int size) {
        URL url = IconUtil.class.getResource("/icons/" + name + ".png");
        if (url == null) {
            return null;
        }
        ImageIcon original = new ImageIcon(url);
        Image scaled = original.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
