package io.github.padalolo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class Tools {

    public static Texture createColorTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public static Texture createColorTexture(Color color, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public static Texture createCircularTexture(Texture sourceTexture, int size) {
        if (!sourceTexture.getTextureData().isPrepared()) {
            sourceTexture.getTextureData().prepare();
        }
        Pixmap sourcePixmap = sourceTexture.getTextureData().consumePixmap();

        int finalSize = Math.max(size, Math.min(sourcePixmap.getWidth(), sourcePixmap.getHeight()));
        Pixmap circularPixmap = new Pixmap(finalSize, finalSize, Pixmap.Format.RGBA8888);
        circularPixmap.setColor(0, 0, 0, 0);
        circularPixmap.fill();

        int radius = finalSize / 2;
        int centerX = radius;
        int centerY = radius;

        int sourceSize = Math.min(sourcePixmap.getWidth(), sourcePixmap.getHeight());
        int sourceOffsetX = (sourcePixmap.getWidth() - sourceSize) / 2;
        int sourceOffsetY = (sourcePixmap.getHeight() - sourceSize) / 2;

        for (int y = 0; y < finalSize; y++) {
            for (int x = 0; x < finalSize; x++) {
                float dx = x - centerX + 0.5f;
                float dy = y - centerY + 0.5f;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance <= radius + 1) {
                    float srcXFloat = ((float) x / finalSize) * sourceSize + sourceOffsetX;
                    float srcYFloat = ((float) y / finalSize) * sourceSize + sourceOffsetY;

                    int srcX = (int) srcXFloat;
                    int srcY = (int) srcYFloat;

                    if (srcX >= 0 && srcX < sourcePixmap.getWidth() && srcY >= 0 && srcY < sourcePixmap.getHeight()) {
                        int color = sourcePixmap.getPixel(srcX, srcY);
                        
                        // Antialiasing pour des bords lisses
                        float alpha = 1.0f;
                        if (distance > radius - 1) {
                            alpha = Math.max(0, radius - distance + 1);
                        }
                        
                        // Extraire les composants RGBA
                        int r = (color >>> 24) & 0xff;
                        int g = (color >>> 16) & 0xff;
                        int b = (color >>> 8) & 0xff;
                        int a = color & 0xff;
                        
                        // Appliquer l'alpha pour l'antialiasing
                        a = (int) (a * alpha);
                        
                        // Recombiner les composants
                        int finalColor = (r << 24) | (g << 16) | (b << 8) | a;
                        circularPixmap.drawPixel(x, y, finalColor);
                    }
                }
            }
        }

        Texture circularTexture = new Texture(circularPixmap);
        circularTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        circularPixmap.dispose();
        sourcePixmap.dispose();

        return circularTexture;
    }
}
