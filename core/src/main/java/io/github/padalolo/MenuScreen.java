package io.github.padalolo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class MenuScreen implements Screen, MusicPlayer.MusicPlayerListener {
    private AssetsManager assetsManager;
    private MusicPlayer musicPlayer;
    private Stage stage;
    private VisTable mainTable;
    private VisTable headerTable;
    private VisTable vinyleTable;
    private VisTable audioControlsTable;
    private Texture headerBgTexture;
    private Texture vinyleTableTexture;
    private Array<Vinyle> vinyleList;
    private int currentAlbumIndex;
    private VisLabel currentTrackLabel;
    private VisLabel playPauseButton;
    private VisLabel elapsedTimeLabel;
    private float vinylY = 125;
    
    public MenuScreen() {
        this.assetsManager = AssetsManager.getInstance();
        this.musicPlayer = MusicPlayer.getInstance();
        this.musicPlayer.setListener(this);
        this.currentAlbumIndex = 0;
        this.vinyleList = new Array<>();
        this.initializeUI();
    }

    private void initializeUI() {
        this.stage = new Stage(new ScreenViewport());
        this.mainTable = new VisTable();
        this.mainTable.setFillParent(true);
        this.stage.addActor(this.mainTable);

        // Load fonts once at the beginning
        this.assetsManager.loadFont(16);

        this.headerTable = createHeader();
        this.mainTable.top();
        this.mainTable.add(this.headerTable).expandX().fillX().height(30).row();

        this.vinyleTable = createVinyleTable();
        this.mainTable.add(this.vinyleTable).expandX().fillX().height(110).padTop(10).row();

        this.audioControlsTable = createAudioControls();
        this.mainTable.add(this.audioControlsTable).expandX().fillX().height(130).padTop(10).row();
        
        // Load first album if available
        if (assetsManager.getAlbumsList().size > 0) {
            String firstAlbumName = assetsManager.getAlbumsList().get(0);
            Album album = assetsManager.loadAlbum(firstAlbumName);
            musicPlayer.loadAlbum(album);
        }
    }

    private VisTable createHeader() {
        VisTable table = new VisTable();
        headerBgTexture = Tools.createColorTexture(new Color(0.1f, 0.1f, 0.1f, 1));
        table.setBackground(new TextureRegionDrawable(headerBgTexture));
        table.setTouchable(Touchable.enabled);

        LabelStyle fontStyle = new LabelStyle();
        fontStyle.font = this.assetsManager.getFont();

        VisLabel titleLabel = new VisLabel("PadaZik", fontStyle);
        table.add(titleLabel).expandX().left().pad(10);

        VisLabel quitLabel = new VisLabel("X", fontStyle);
        quitLabel.addListener(new QuitButtonListener(quitLabel));
        table.add(quitLabel).expandX().right().pad(10);

        table.addListener(new WindowDragListener());
        return table;
    }

    private VisTable createVinyleTable() {
        VisTable table = new VisTable();

        int vinylSize = 120;
        
        float increment = 0;
        int index = 0;

        for(Texture albumTexture : this.assetsManager.getAlbumImageList()){
            float x = Gdx.graphics.getWidth() / 2 - vinylSize / 2;
            float y = vinylY;
            if (albumTexture != null) {
                Vinyle vinyle = new Vinyle(Tools.createCircularTexture(albumTexture, vinylSize), x, y);
                vinyle.setSize(vinylSize, vinylSize);
                vinyle.setPosition(x + increment, y);
                
                // Store album reference
                String albumName = this.assetsManager.getAlbumsList().get(index);
                Album album = new Album(albumName);
                vinyle.setAlbum(album);
                
                // Make vinyl clickable
                final int albumIndex = index;
                vinyle.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        switchToAlbum(albumIndex);
                    }
                });
                
                this.vinyleList.add(vinyle);
                this.stage.addActor(vinyle);
            }
            increment += 60; // Reduced spacing for overlapping effect
            index++;
        }
        
        // Start rotation for the first vinyl (center)
        if (vinyleList.size > 0) {
            updateVinylPositions();
        }
        
        return table;
    }

    private VisTable createAudioControls() {
        VisTable table = new VisTable();
        Texture controlsBgTexture = Tools.createColorTexture(new Color(0.15f, 0.15f, 0.15f, 1));
        table.setBackground(new TextureRegionDrawable(controlsBgTexture));

        LabelStyle fontStyle = new LabelStyle();
        fontStyle.font = this.assetsManager.getFont();

        // Current track label
        this.currentTrackLabel = new VisLabel("No track", fontStyle);
        table.add(this.currentTrackLabel).expandX().center().padTop(10).row();

        // Elapsed time label (centered)
        this.elapsedTimeLabel = new VisLabel("0:00", fontStyle);
        table.add(this.elapsedTimeLabel).expandX().center().padTop(5).row();

        // Control buttons
        VisTable buttonsTable = new VisTable();
        
        // Previous button
        VisLabel previousButton = new VisLabel("<<", fontStyle);
        previousButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicPlayer.previous();
            }
        });
        buttonsTable.add(previousButton).pad(10);

        // Play/Pause button
        this.playPauseButton = new VisLabel("PLAY", fontStyle);
        this.playPauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicPlayer.togglePlayPause();
            }
        });
        buttonsTable.add(this.playPauseButton).pad(10);

        // Next button
        VisLabel nextButton = new VisLabel(">>", fontStyle);
        nextButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicPlayer.next();
            }
        });
        buttonsTable.add(nextButton).pad(10);

        table.add(buttonsTable).expandX().center().padTop(5).row();

        // Volume control - minimalist slider with custom style
        com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle sliderStyle = new com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle();
        
        // Create smaller knob (6x6 pixels)
        Texture knobTexture = Tools.createColorTexture(new Color(0.7f, 0.7f, 0.7f, 1), 6, 6);
        sliderStyle.knob = new TextureRegionDrawable(knobTexture);
        
        // Create background bar
        Texture bgTexture = Tools.createColorTexture(new Color(0.3f, 0.3f, 0.3f, 1), 1, 3);
        sliderStyle.background = new TextureRegionDrawable(bgTexture);
        
        // Create knob before (filled part)
        Texture knobBeforeTexture = Tools.createColorTexture(new Color(0.6f, 0.6f, 0.6f, 1), 1, 3);
        sliderStyle.knobBefore = new TextureRegionDrawable(knobBeforeTexture);
        
        com.kotcrab.vis.ui.widget.VisSlider volumeSlider = new com.kotcrab.vis.ui.widget.VisSlider(0, 100, 1, false);
        volumeSlider.setStyle(sliderStyle);
        volumeSlider.setValue(musicPlayer.getVolume() * 100);
        volumeSlider.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                float volume = volumeSlider.getValue() / 100f;
                musicPlayer.setVolume(volume);
            }
        });
        table.add(volumeSlider).width(100).height(10).padTop(5).padBottom(10);

        return table;
    }

    private void switchToAlbum(int albumIndex) {
        if (albumIndex < 0 || albumIndex >= vinyleList.size) {
            return;
        }

        currentAlbumIndex = albumIndex;
        
        // Load the new album
        String albumName = assetsManager.getAlbumsList().get(albumIndex);
        Album album = assetsManager.loadAlbum(albumName);
        musicPlayer.loadAlbum(album);
        
        // Update vinyl positions with animation
        updateVinylPositions();
    }

    private void updateVinylPositions() {
        int vinylSize = 120;
        float centerX = Gdx.graphics.getWidth() / 2 - vinylSize / 2;
        int spacing = 60; // Reduced spacing for overlapping

        // Stop all rotations first
        for (Vinyle vinyle : vinyleList) {
            vinyle.stopRotation();
        }

        // Update positions and z-index based on current album index
        for (int i = 0; i < vinyleList.size; i++) {
            Vinyle vinyle = vinyleList.get(i);
            int offset = i - currentAlbumIndex;
            float newX = centerX + (offset * spacing);
            vinyle.setPosition(newX, vinylY);
            
            // Set z-index: center vinyl is highest, decreasing as we move away
            // For left side: further left = lower z-index
            // For right side: further right = lower z-index
            int zIndex = vinyleList.size - Math.abs(offset);
            vinyle.setZIndex(zIndex);
        }
        
        // Sort actors by z-index to ensure proper rendering order
        stage.getActors().sort((a, b) -> {
            if (a instanceof Vinyle && b instanceof Vinyle) {
                return Integer.compare(((Vinyle) a).getZIndex(), ((Vinyle) b).getZIndex());
            }
            return 0;
        });

        // Start rotation for center vinyl only (no auto-play on load)
        // Rotation will start when play button is clicked
    }

    // MusicPlayerListener implementation
    @Override
    public void onTrackChanged(String trackTitle) {
        if (currentTrackLabel != null) {
            currentTrackLabel.setText(trackTitle);
        }
    }

    @Override
    public void onPlayStateChanged(boolean isPlaying) {
        if (playPauseButton != null) {
            playPauseButton.setText(isPlaying ? "PAUSE" : "PLAY");
        }
        
        // Update vinyl rotation
        if (vinyleList.size > currentAlbumIndex) {
            Vinyle currentVinyl = vinyleList.get(currentAlbumIndex);
            if (isPlaying) {
                currentVinyl.startRotation();
            } else {
                currentVinyl.stopRotation();
            }
        }
    }

    @Override
    public void onAlbumChanged(Album album) {
        // Update UI when album changes
        if (album != null && album.getCurrentTrack() != null) {
            onTrackChanged(album.getCurrentTrack().getTitle());
        } else {
            onTrackChanged("No track");
        }
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
        
        // Update progress bar
        updateProgressBar();
    }
    
    private void updateProgressBar() {
        Music music = musicPlayer.getCurrentMusic();
        if (music != null) {
            float position = music.getPosition();
            
            // Update elapsed time label
            if (elapsedTimeLabel != null) {
                elapsedTimeLabel.setText(formatTime(position));
            }
            
            // Check if music has finished playing
            // When a music finishes, isPlaying() returns false even though we think it's playing
            if (musicPlayer.isPlaying() && !music.isPlaying()) {
                // Music has finished, play next track
                musicPlayer.next();
            }
        } else {
            // Reset when no music is playing
            if (elapsedTimeLabel != null) {
                elapsedTimeLabel.setText("0:00");
            }
        }
    }
    
    private String formatTime(float seconds) {
        int minutes = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", minutes, secs);
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
        if (vinyleTableTexture != null) {
            vinyleTableTexture.dispose();
        }
        if (musicPlayer != null) {
            musicPlayer.dispose();
        }
    }
}