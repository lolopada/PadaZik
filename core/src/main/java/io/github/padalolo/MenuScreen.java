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
    // UI Constants
    private static final int VINYL_SIZE = 120;
    private static final int VINYL_SPACING = 60;
    private static final float VINYL_Y = 125;
    private static final float VINYL_ANIMATION_DURATION = 0.4f;
    private static final int FONT_SIZE = 16;
    private static final int SLIDER_WIDTH = 100;
    private static final int SLIDER_HEIGHT = 10;
    private static final int SLIDER_KNOB_SIZE = 6;
    private static final int MAX_TITLE_LENGTH = 25;
    private static final String SCROLL_SEPARATOR = "   ";
    
    private AssetsManager assetsManager;
    private MusicPlayer musicPlayer;
    private Stage stage;
    private VisTable mainTable;
    private VisTable headerTable;
    private VisTable vinyleTable;
    private VisTable audioControlsTable;
    private Texture headerBgTexture;
    private Texture controlsBgTexture;
    private Array<Vinyle> vinyleList;
    private int currentAlbumIndex;
    private VisLabel currentTrackLabel;
    private VisLabel playPauseButton;
    private VisLabel elapsedTimeLabel;
    private LabelStyle sharedLabelStyle;
    
    // Scrolling text state
    private String fullTrackTitle;
    private String scrollingText;
    private float scrollOffset;
    private boolean shouldScroll;
    
    // Slider textures for proper disposal
    private Texture sliderKnobTexture;
    private Texture sliderBgTexture;
    private Texture sliderKnobBeforeTexture;
    
    // Cache for circular textures
    private Array<Texture> circularTexturesCache;
    
    public MenuScreen() {
        this.assetsManager = AssetsManager.getInstance();
        this.musicPlayer = MusicPlayer.getInstance();
        this.musicPlayer.setListener(this);
        this.currentAlbumIndex = 0;
        this.vinyleList = new Array<>();
        this.circularTexturesCache = new Array<>();
        this.initializeUI();
    }

    private void initializeUI() {
        this.stage = new Stage(new ScreenViewport());
        this.mainTable = new VisTable();
        this.mainTable.setFillParent(true);
        this.stage.addActor(this.mainTable);

        // Load fonts once at the beginning
        this.assetsManager.loadFont(FONT_SIZE);
        
        // Create shared label style
        this.sharedLabelStyle = new LabelStyle();
        this.sharedLabelStyle.font = this.assetsManager.getFont();

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

        VisLabel titleLabel = new VisLabel("PadaZik", sharedLabelStyle);
        table.add(titleLabel).expandX().left().pad(10);

        VisLabel quitLabel = new VisLabel("X", sharedLabelStyle);
        quitLabel.addListener(new QuitButtonListener(quitLabel));
        table.add(quitLabel).expandX().right().pad(10);

        table.addListener(new WindowDragListener());
        return table;
    }

    private VisTable createVinyleTable() {
        VisTable table = new VisTable();
        
        float increment = 0;
        int index = 0;

        for(Texture albumTexture : this.assetsManager.getAlbumImageList()){
            float x = Gdx.graphics.getWidth() / 2 - VINYL_SIZE / 2;
            float y = VINYL_Y;
            if (albumTexture != null) {
                // Create circular texture and cache it
                Texture circularTexture = Tools.createCircularTexture(albumTexture, VINYL_SIZE);
                circularTexturesCache.add(circularTexture);
                
                Vinyle vinyle = new Vinyle(circularTexture, x, y);
                vinyle.setSize(VINYL_SIZE, VINYL_SIZE);
                vinyle.setPosition(x + increment, y);
                
                // Store album index reference instead of creating new Album
                vinyle.setAlbumIndex(index);
                
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
        controlsBgTexture = Tools.createColorTexture(new Color(0.15f, 0.15f, 0.15f, 1));
        table.setBackground(new TextureRegionDrawable(controlsBgTexture));

        // Current track label
        this.currentTrackLabel = new VisLabel("No track", sharedLabelStyle);
        table.add(this.currentTrackLabel).expandX().center().padTop(10).row();

        // Elapsed time label (centered)
        this.elapsedTimeLabel = new VisLabel("0:00", sharedLabelStyle);
        table.add(this.elapsedTimeLabel).expandX().center().padTop(5).row();

        // Control buttons
        VisTable buttonsTable = new VisTable();
        
        // Previous button
        VisLabel previousButton = new VisLabel("<<", sharedLabelStyle);
        previousButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicPlayer.previous();
            }
        });
        buttonsTable.add(previousButton).pad(10);

        // Play/Pause button
        this.playPauseButton = new VisLabel("PLAY", sharedLabelStyle);
        this.playPauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicPlayer.togglePlayPause();
            }
        });
        buttonsTable.add(this.playPauseButton).pad(10);

        // Next button
        VisLabel nextButton = new VisLabel(">>", sharedLabelStyle);
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
        
        // Create smaller knob and store for disposal
        sliderKnobTexture = Tools.createColorTexture(new Color(0.7f, 0.7f, 0.7f, 1), SLIDER_KNOB_SIZE, SLIDER_KNOB_SIZE);
        sliderStyle.knob = new TextureRegionDrawable(sliderKnobTexture);
        
        // Create background bar and store for disposal
        sliderBgTexture = Tools.createColorTexture(new Color(0.3f, 0.3f, 0.3f, 1), 1, 3);
        sliderStyle.background = new TextureRegionDrawable(sliderBgTexture);
        
        // Create knob before (filled part) and store for disposal
        sliderKnobBeforeTexture = Tools.createColorTexture(new Color(0.6f, 0.6f, 0.6f, 1), 1, 3);
        sliderStyle.knobBefore = new TextureRegionDrawable(sliderKnobBeforeTexture);
        
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
        table.add(volumeSlider).width(SLIDER_WIDTH).height(SLIDER_HEIGHT).padTop(5).padBottom(10);

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
        float centerX = Gdx.graphics.getWidth() / 2 - VINYL_SIZE / 2;

        // Stop all rotations first
        for (Vinyle vinyle : vinyleList) {
            vinyle.stopRotation();
        }

        // Update positions and z-index based on current album index
        for (int i = 0; i < vinyleList.size; i++) {
            Vinyle vinyle = vinyleList.get(i);
            int offset = i - currentAlbumIndex;
            float newX = centerX + (offset * VINYL_SPACING);
            
            // Animate to position with smooth effect
            vinyle.animateToPosition(newX, VINYL_Y, VINYL_ANIMATION_DURATION);
            
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
            fullTrackTitle = trackTitle;
            scrollOffset = 0;
            
            if (trackTitle.length() > MAX_TITLE_LENGTH) {
                // Create scrolling text with separator
                scrollingText = trackTitle + SCROLL_SEPARATOR + trackTitle;
                shouldScroll = true;
                currentTrackLabel.setText(trackTitle.substring(0, Math.min(MAX_TITLE_LENGTH, trackTitle.length())));
            } else {
                scrollingText = trackTitle;
                shouldScroll = false;
                currentTrackLabel.setText(trackTitle);
            }
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
        
        // Update scrolling text
        updateScrollingText(delta);
    }
    
    private void updateProgressBar() {
        Music music = musicPlayer.getCurrentMusic();
        if (music != null) {
            float position = music.getPosition();
            
            // Update elapsed time label
            if (elapsedTimeLabel != null) {
                elapsedTimeLabel.setText(formatTime(position));
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
    
    private void updateScrollingText(float delta) {
        if (!shouldScroll || currentTrackLabel == null || fullTrackTitle == null) {
            return;
        }
        
        // Increment scroll position smoothly
        scrollOffset += delta * 1f; // Slower, smoother scrolling (0.5 characters per second)
        
        // Reset when we've scrolled through one full cycle
        if (scrollOffset >= fullTrackTitle.length() + SCROLL_SEPARATOR.length()) {
            scrollOffset -= (fullTrackTitle.length() + SCROLL_SEPARATOR.length());
        }
        
        // Build visible text with smooth character-based scrolling
        StringBuilder visibleText = new StringBuilder();
        int startIndex = (int) scrollOffset;
        
        for (int i = 0; i < MAX_TITLE_LENGTH; i++) {
            int charIndex = (startIndex + i) % scrollingText.length();
            visibleText.append(scrollingText.charAt(charIndex));
        }
        
        currentTrackLabel.setText(visibleText.toString());
    }

    @Override
    public void resize(int width, int height) {
        this.stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        this.stage.dispose();
        
        // Dispose header texture
        if (headerBgTexture != null) {
            headerBgTexture.dispose();
        }
        
        // Dispose controls background texture
        if (controlsBgTexture != null) {
            controlsBgTexture.dispose();
        }
        
        // Dispose slider textures
        if (sliderKnobTexture != null) {
            sliderKnobTexture.dispose();
        }
        if (sliderBgTexture != null) {
            sliderBgTexture.dispose();
        }
        if (sliderKnobBeforeTexture != null) {
            sliderKnobBeforeTexture.dispose();
        }
        
        // Dispose cached circular textures
        if (circularTexturesCache != null) {
            for (Texture texture : circularTexturesCache) {
                if (texture != null) {
                    texture.dispose();
                }
            }
            circularTexturesCache.clear();
        }
        
        if (musicPlayer != null) {
            musicPlayer.dispose();
        }
    }
}