package io.github.padalolo;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.VisUI;

public class Main extends Game {
    private AssetsManager assetsManager;
    private MenuScreen menuScreen;

    @Override
    public void create() {
        VisUI.load();
        this.assetsManager = AssetsManager.getInstance();
        this.assetsManager.setupAlbum();

        this.menuScreen = new MenuScreen();
        setScreen(this.menuScreen);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.assetsManager != null) {
            this.assetsManager.dispose();
        }
        VisUI.dispose();
    }

    public MenuScreen getMenuScreen() {
        return this.menuScreen;
    }
}
