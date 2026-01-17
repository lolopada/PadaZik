package io.github.padalolo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
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
    private Texture vinylTexture;
    private Array<String> albumsList;
    private Array<Texture> albumImageList;
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

    public Texture getVinylTexture() {
        if (this.vinylTexture == null) {
            this.vinylTexture = new Texture(Gdx.files.internal(VINYL_IMAGE_PATH), true);
            this.vinylTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        }
        return this.vinylTexture;
    }

    public void setupAlbum() {
        Array<String> folders = new Array<>();
        Array<Texture> images = new Array<>();

        FileHandle assetsDir = Gdx.files.absolute(System.getenv("LOCALAPPDATA") + "/PadaZik/assets");
        System.out.println("Assets directory path: " + assetsDir.path());

        if (assetsDir.exists() && assetsDir.isDirectory()) {
            FileHandle[] files = assetsDir.list();

            for (FileHandle file : files) {
                if (file.isDirectory()) {
                    folders.add(file.name());

                    FileHandle imageFile = file.child("image.png");
                    if (imageFile.exists()) {
                        try {
                            Texture albumTexture = new Texture(imageFile);
                            images.add(albumTexture);
                        } catch (Exception e) {
                            System.err.println("Erreur lors du chargement de l'image pour " + file.name() + ": " + e.getMessage());
                            // Use fallback vinyl image
                            images.add(getVinylTexture());
                        }
                    } else {
                        System.out.println("Aucune image trouvée pour l'album: " + file.name());
                        // Use fallback vinyl image
                        images.add(getVinylTexture());
                    }
                }
            }
        }

        this.albumsList = folders;
        this.albumImageList = images;
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

    public Array<String> getAlbumsList() {
        return this.albumsList;
    }

    public Array<Texture> getAlbumImageList() {
        return this.albumImageList;
    }

    public boolean isAlbumLoaded() {
        return this.currentAlbum != null;
    }

    public int getAlbumsCount() {
        return this.albumsList.size;
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
        if (this.albumImageList != null) {
            for (Texture texture : this.albumImageList) {
                if (texture != null) {
                    texture.dispose();
                }
            }
            this.albumImageList.clear();
        }
        this.unloadCurrentAlbum();
        this.loadedFontSize = -1;
    }
}
