package io.github.padalolo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class AssetsManager implements Disposable {
    private static final String FONT_PATH = "Agbalumo-Regular.ttf";
    private static final String VINYL_IMAGE_PATH = "Vinyle.png";
    private static AssetsManager instance;
    private BitmapFont font;
    private int loadedFontSize = -1;
    private com.badlogic.gdx.graphics.Texture vinylTexture;
    private Album currentAlbum;

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

    public com.badlogic.gdx.graphics.Texture getVinylTexture() {
        if (this.vinylTexture == null) {
            this.vinylTexture = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal(VINYL_IMAGE_PATH), true);
            this.vinylTexture.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.MipMapLinearLinear,
                    com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
        }
        return this.vinylTexture;
    }

    public Array<String> getAllFoldersInAssets() {
        Array<String> folders = new Array<>();
        FileHandle assetsDir = Gdx.files.internal("");
        
        if (assetsDir.exists() && assetsDir.isDirectory()) {
            for (FileHandle file : assetsDir.list()) {
                if (file.isDirectory()) {
                    folders.add(file.name());
                }
            }
        }
        
        return folders;
    }

    public Album loadAlbum(String albumName) {
        if (this.currentAlbum != null) {
            if (this.currentAlbum.getName().equals(albumName)) {
                return this.currentAlbum;
            }
            unloadCurrentAlbum();
        }
        
        this.currentAlbum = new Album(albumName);
        return this.currentAlbum;
    }

    public void unloadCurrentAlbum() {
        if (this.currentAlbum != null) {
            this.currentAlbum.dispose();
            this.currentAlbum = null;
        }
    }

    public Album getCurrentAlbum() {
        return this.currentAlbum;
    }

    public boolean isAlbumLoaded() {
        return this.currentAlbum != null;
    }

    @Override
    public void dispose() {
        instance = null;
        if (this.font != null) {
            this.font.dispose();
        }
        if (this.vinylTexture != null) {
            this.vinylTexture.dispose();
        }
        this.unloadCurrentAlbum();
        this.loadedFontSize = -1;
    }
}
