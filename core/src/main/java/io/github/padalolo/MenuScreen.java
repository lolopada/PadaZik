package io.github.padalolo;

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
    private AssetsManager assetsManager;
    private Stage stage;

    private VisTable mainTable;
    private VisTable headerTable;
    private VisTable VinyleTable;

    private Texture headerBgTexture;
    private Texture VinyleTableTexture;

    public MenuScreen() {
        this.assetsManager = AssetsManager.getInstance();
        this.initializeUI();
    }

    private void initializeUI() {
        this.stage = new Stage(new ScreenViewport());

        this.mainTable = new VisTable();
        this.mainTable.setFillParent(true);
        this.stage.addActor(this.mainTable);

        this.headerTable = this.createHeader();

        this.mainTable.top();
        this.mainTable.add(this.headerTable).expandX().fillX().height(30).row();

        this.VinyleTable = this.createVinyleTable();
        this.mainTable.add(this.VinyleTable).expandX().fillX().height(110).padTop(10).row();

    }

    private VisTable createHeader() {
        VisTable table = new VisTable();
        headerBgTexture = createColorTexture(new Color(0.1f, 0.1f, 0.1f, 1));

        table.setBackground(new TextureRegionDrawable(headerBgTexture));
        table.setTouchable(Touchable.enabled);

        this.assetsManager.loadFont(16);
        LabelStyle fontStyle = new LabelStyle();
        fontStyle.font = this.assetsManager.getFont();

        // Title
        VisLabel titleLabel = new VisLabel("PadaZik", fontStyle);
        table.add(titleLabel).expandX().left().pad(10);

        // Quit label
        VisLabel quitLabel = new VisLabel("X", fontStyle);
        quitLabel.addListener(new QuitButtonListener(quitLabel));
        table.add(quitLabel).expandX().right().pad(10);

        table.addListener(new WindowDragListener());
        return table;
    }

    private VisTable createVinyleTable() {
        VisTable table = new VisTable();
        this.VinyleTableTexture = createColorTexture(new Color(0.2f, 0.2f, 0.2f, 1));
        table.setBackground(new TextureRegionDrawable(this.VinyleTableTexture));

        Texture vinylTexture = this.assetsManager.getVinylTexture();

        float vinylY = 70;

        // Vinyle gauche - partiellement hors écran
        com.kotcrab.vis.ui.widget.VisImage leftVinyl = new com.kotcrab.vis.ui.widget.VisImage(vinylTexture);
        leftVinyl.setSize(130, 130);
        leftVinyl.setPosition(-70, vinylY); // Plus écarté à gauche
        this.stage.addActor(leftVinyl);

        // Vinyle central
        com.kotcrab.vis.ui.widget.VisImage centerVinyl = new com.kotcrab.vis.ui.widget.VisImage(vinylTexture);
        centerVinyl.setSize(130, 130);
        centerVinyl.setPosition(Gdx.graphics.getWidth() / 2 - 65, vinylY); // Centré (130/2 = 65)
        this.stage.addActor(centerVinyl);

        // Vinyle droit - partiellement hors écran
        com.kotcrab.vis.ui.widget.VisImage rightVinyl = new com.kotcrab.vis.ui.widget.VisImage(vinylTexture);
        rightVinyl.setSize(130, 130);
        rightVinyl.setPosition(Gdx.graphics.getWidth() - 60, vinylY); // Plus écarté à droite
        this.stage.addActor(rightVinyl);

        return table;
    }

    private Texture createColorTexture(Color c) {
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