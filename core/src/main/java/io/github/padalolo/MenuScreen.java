package io.github.padalolo;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class MenuScreen implements Screen {
    private final Game game;
    private AssetsManager assetsManager;
    private Stage stage;
    private Texture headerBgTexture;

    public MenuScreen(Game game) {
        this.game = game;
        this.assetsManager = AssetsManager.getInstance();
        this.initializeUI();
    }

    private void initializeUI() {
        this.stage = new Stage(new ScreenViewport());

        VisTable mainTable = new VisTable();
        mainTable.setFillParent(true);
        this.stage.addActor(mainTable);

        VisTable headerTable = new VisTable();
        if (headerBgTexture == null) {
            headerBgTexture = createHeaderBgTexture(new Color(0.1f, 0.1f, 0.1f, 1));
        }
        headerTable.setBackground(new TextureRegionDrawable(headerBgTexture));
        headerTable.setTouchable(Touchable.enabled);

        this.assetsManager.loadFont(16);
        LabelStyle fontStyle = new LabelStyle();
        fontStyle.font = this.assetsManager.getFont();

        // Title
        VisLabel titleLabel = new VisLabel("PadaZik", fontStyle);
        headerTable.add(titleLabel).expandX().left().pad(10);

        // Quit label
        VisLabel quitLabel = new VisLabel("X", fontStyle);
        quitLabel.addListener(new QuitButtonListener());
        headerTable.add(quitLabel).expandX().right().pad(10);

        headerTable.addListener(new WindowDragListener());
        mainTable.top();
        mainTable.add(headerTable).expandX().fillX().height(30).row();
    }

    private Texture createHeaderBgTexture(Color c) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(c);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this.stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        this.stage.act(delta);
        this.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        this.stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        this.stage.dispose();
        if (headerBgTexture != null) {
            headerBgTexture.dispose();
        }
    }
}