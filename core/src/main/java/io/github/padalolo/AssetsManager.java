package io.github.padalolo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;

public class AssetsManager implements Disposable {
    private static final String FONT_PATH = "Agbalumo-Regular.ttf";
    private static AssetsManager instance;
    private BitmapFont font;
    private int loadedFontSize = -1;

    public static AssetsManager getInstance() {
        if (instance == null) {
            instance = new AssetsManager();
        }
        return instance;
    }

    public BitmapFont loadFont(int fontSize) {
        if (this.font == null || this.loadedFontSize != fontSize) {
            if (this.font != null) {
                this.font.dispose();
            }
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = fontSize;
            this.font = generator.generateFont(parameter);
            this.loadedFontSize = fontSize;
            generator.dispose();
        }
        return this.font;
    }

    public BitmapFont getFont() {
        return this.font;
    }

    @Override
    public void dispose() {
        instance = null;
        if (font != null) {
            font.dispose();
        }
        loadedFontSize = -1;
    }
}
