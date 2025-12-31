package io.github.padalolo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class Album implements Disposable {
    private final String name;
    private final Array<MusicTrack> tracks;
    private int currentTrackIndex;
    private Texture albumImage;

    public Album(String albumName) {
        this.name = albumName;
        this.tracks = new Array<>();
        this.currentTrackIndex = 0;
        loadTracks();
    }

    private void loadTracks() {
        FileHandle albumDir = Gdx.files.internal(name);
        
        if (albumDir.exists() && albumDir.isDirectory()) {
            FileHandle imageFile = albumDir.child("image.png");
            if (imageFile.exists()) {
                try {
                    this.albumImage = new Texture(imageFile);
                } catch (Exception e) {
                    this.albumImage = null;
                }
            } else {
                this.albumImage = null;
            }
            
            for (FileHandle file : albumDir.list()) {
                if (file.extension().equalsIgnoreCase("mp3")) {
                    tracks.add(new MusicTrack(file.nameWithoutExtension(), file.path()));
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public Array<MusicTrack> getTracks() {
        return tracks;
    }

    public int getTrackCount() {
        return tracks.size;
    }

    public MusicTrack getCurrentTrack() {
        if (tracks.size > 0 && currentTrackIndex >= 0 && currentTrackIndex < tracks.size) {
            return tracks.get(currentTrackIndex);
        }
        return null;
    }

    public MusicTrack getTrack(int index) {
        if (index >= 0 && index < tracks.size) {
            return tracks.get(index);
        }
        return null;
    }

    public void setCurrentTrackIndex(int index) {
        if (index >= 0 && index < tracks.size) {
            this.currentTrackIndex = index;
        }
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public Texture getAlbumImage() {
        return albumImage;
    }

    public MusicTrack nextTrack() {
        if (tracks.size > 0) {
            currentTrackIndex = (currentTrackIndex + 1) % tracks.size;
            return getCurrentTrack();
        }
        return null;
    }

    public MusicTrack previousTrack() {
        if (tracks.size > 0) {
            currentTrackIndex = (currentTrackIndex - 1 + tracks.size) % tracks.size;
            return getCurrentTrack();
        }
        return null;
    }

    @Override
    public void dispose() {
        for (MusicTrack track : tracks) {
            track.dispose();
        }
        tracks.clear();
        
        if (albumImage != null) {
            albumImage.dispose();
            albumImage = null;
        }
    }

    public static class MusicTrack implements Disposable {
        private final String title;
        private final String filePath;
        private Music music;

        public MusicTrack(String title, String filePath) {
            this.title = title;
            this.filePath = filePath;
        }

        public String getTitle() {
            return title;
        }

        public String getFilePath() {
            return filePath;
        }

        public Music getMusic() {
            if (music == null) {
                music = Gdx.audio.newMusic(Gdx.files.internal(filePath));
            }
            return music;
        }

        public boolean isLoaded() {
            return music != null;
        }

        @Override
        public void dispose() {
            if (music != null) {
                music.dispose();
                music = null;
            }
        }
    }
}
