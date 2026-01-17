package io.github.padalolo;

import com.badlogic.gdx.audio.Music;

public class MusicPlayer {
    private static MusicPlayer instance;
    private Album currentAlbum;
    private Music currentMusic;
    private boolean isPlaying;
    private MusicPlayerListener listener;
    private float volume;

    public interface MusicPlayerListener {
        void onTrackChanged(String trackTitle);
        void onPlayStateChanged(boolean isPlaying);
        void onAlbumChanged(Album album);
    }

    private MusicPlayer() {
        this.isPlaying = false;
        this.volume = 0.5f;
    }

    public static MusicPlayer getInstance() {
        if (instance == null) {
            instance = new MusicPlayer();
        }
        return instance;
    }

    public void setListener(MusicPlayerListener listener) {
        this.listener = listener;
    }

    public void loadAlbum(Album album) {
        // Stop current music if playing
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
        
        // Reset playing state
        boolean wasPlaying = isPlaying;
        isPlaying = false;
        
        // Load new album
        this.currentAlbum = album;
        
        // Notify listeners
        if (listener != null) {
            // Notify play state changed if we were playing
            if (wasPlaying) {
                listener.onPlayStateChanged(false);
            }
            
            // Notify album changed
            listener.onAlbumChanged(album);
            
            // Update track title
            if (album != null && album.getCurrentTrack() != null) {
                listener.onTrackChanged(album.getCurrentTrack().getTitle());
            } else {
                listener.onTrackChanged("No track");
            }
        }
    }

    public void play() {
        if (currentAlbum == null || currentAlbum.getTrackCount() == 0) {
            return;
        }

        Album.MusicTrack track = currentAlbum.getCurrentTrack();
        if (track == null) {
            return;
        }

        if (currentMusic == null) {
            currentMusic = track.getMusic();
            currentMusic.setVolume(volume);
            
            // Set completion listener to auto-play next track
            currentMusic.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                    next();
                }
            });
        }

        if (!currentMusic.isPlaying()) {
            currentMusic.play();
            isPlaying = true;
            if (listener != null) {
                listener.onPlayStateChanged(true);
                listener.onTrackChanged(track.getTitle());
            }
        }
    }

    public void pause() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
            isPlaying = false;
            if (listener != null) {
                listener.onPlayStateChanged(false);
            }
        }
    }

    public void togglePlayPause() {
        if (isPlaying) {
            pause();
        } else {
            play();
        }
    }

    public void next() {
        if (currentAlbum == null) {
            return;
        }

        Album.MusicTrack nextTrack = currentAlbum.nextTrack();
        playTrack(nextTrack);
    }

    public void previous() {
        if (currentAlbum == null) {
            return;
        }

        Album.MusicTrack prevTrack = currentAlbum.previousTrack();
        playTrack(prevTrack);
    }
    
    /**
     * Helper method to play a specific track
     * Stops current music and starts playing the new track
     */
    private void playTrack(Album.MusicTrack track) {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }

        if (track != null) {
            currentMusic = track.getMusic();
            currentMusic.setVolume(volume);
            
            // Set completion listener to auto-play next track
            currentMusic.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                    next();
                }
            });
            
            currentMusic.play();
            isPlaying = true;
            if (listener != null) {
                listener.onPlayStateChanged(true);
                listener.onTrackChanged(track.getTitle());
            }
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public Album getCurrentAlbum() {
        return currentAlbum;
    }

    public String getCurrentTrackTitle() {
        if (currentAlbum != null && currentAlbum.getCurrentTrack() != null) {
            return currentAlbum.getCurrentTrack().getTitle();
        }
        return "No track";
    }

    public Music getCurrentMusic() {
        return currentMusic;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(this.volume);
        }
    }

    public float getVolume() {
        return volume;
    }

    public void dispose() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
        if (currentAlbum != null) {
            currentAlbum = null;
        }
        isPlaying = false;
        listener = null;
        instance = null;
    }
}
