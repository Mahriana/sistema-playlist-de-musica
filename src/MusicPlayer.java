import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MusicPlayer extends PlaybackListener {
    private static final Object playSignal = new Object();

    private MusicPlayerGUI musicPlayerGUI;
    private Musica currentSong;
    private Playlist<Musica> playlist;
    private Stack<Musica> historyStack;
    private int currentPlaylistIndex;
    private AdvancedPlayer advancedPlayer;
    private boolean isPaused;
    private boolean songFinished;
    private boolean pressedNext, pressedPrev;
    private int currentFrame;
    private int currentTimeInMilli;

    public MusicPlayer(MusicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
        this.historyStack = new Stack<>();
    }

    public Musica getCurrentSong() {
        return currentSong;
    }

    public Playlist<Musica> getPlaylist() {
        return playlist;
    }

    public void loadSong(Musica song) {
        currentSong = song;
        playlist = null;

        if (!songFinished) stopSong();

        if (currentSong != null) {
            currentFrame = 0;
            currentTimeInMilli = 0;
            musicPlayerGUI.setPlaybackSliderValue(0);
            historyStack.push(currentSong);
            playCurrentSong();
        }
    }

    public void loadPlaylist(File playlistFile) {
        playlist = new Playlist<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(playlistFile))) {
            String songPath;
            while ((songPath = bufferedReader.readLine()) != null) {
                playlist.addItem(new Musica(songPath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (playlist.size() > 0) {
            musicPlayerGUI.setPlaybackSliderValue(0);
            currentTimeInMilli = 0;
            currentSong = playlist.getItem(0);
            currentFrame = 0;
            historyStack.push(currentSong);
            musicPlayerGUI.enablePauseButtonDisablePlayButton();
            musicPlayerGUI.updateSongTitleAndArtist(currentSong);
            musicPlayerGUI.updatePlaybackSlider(currentSong);
            playCurrentSong();
        }
    }

    public void pauseSong() {
        if (advancedPlayer != null) {
            isPaused = true;
            stopSong();
        }
    }

    public void stopSong() {
        if (advancedPlayer != null) {
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }
    }

    public void nextSong() {
        if (playlist == null || currentPlaylistIndex + 1 > playlist.size() - 1) return;

        pressedNext = true;
        if (!songFinished) stopSong();

        currentPlaylistIndex++;
        currentSong = playlist.getItem(currentPlaylistIndex);
        currentFrame = 0;
        currentTimeInMilli = 0;
        historyStack.push(currentSong);
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);
        playCurrentSong();
    }

    public void prevSong() {
        if (playlist == null || currentPlaylistIndex - 1 < 0) return;

        pressedPrev = true;
        if (!songFinished) stopSong();

        currentPlaylistIndex--;
        currentSong = playlist.getItem(currentPlaylistIndex);
        currentFrame = 0;
        currentTimeInMilli = 0;
        historyStack.push(currentSong);
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);
        playCurrentSong();
    }

    public void playCurrentSong() {
        if (currentSong == null) return;

        try {
            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);
            startMusicThread();
            startPlaybackSliderThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startMusicThread() {
        new Thread(() -> {
            try {
                if (isPaused) {
                    synchronized (playSignal) {
                        isPaused = false;
                        playSignal.notify();
                    }
                    advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
                } else {
                    advancedPlayer.play();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startPlaybackSliderThread() {
        new Thread(() -> {
            if (isPaused) {
                try {
                    synchronized (playSignal) {
                        playSignal.wait();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            while (!isPaused && !songFinished && !pressedNext && !pressedPrev) {
                try {
                    currentTimeInMilli++;
                    int calculatedFrame = (int) ((double) currentTimeInMilli * 2.08 * currentSong.getFrameRatePerMilliseconds());
                    musicPlayerGUI.setPlaybackSliderValue(calculatedFrame);
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        System.out.println("Playback Started");
        songFinished = false;
        pressedNext = false;
        pressedPrev = false;
    }

    @Override
public void playbackFinished(PlaybackEvent evt) {
    System.out.println("Playback Finished");
    if (isPaused) {
        currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
    } else {
        if (pressedNext || pressedPrev) return;

        songFinished = true;

        if (playlist == null) {
            musicPlayerGUI.enablePlayButtonDisablePauseButton();
        } else {
            if (currentPlaylistIndex == playlist.size() - 1) {
                // Última música da playlist
                musicPlayerGUI.enablePlayButtonDisablePauseButton();
            } else {
                // Toca a próxima música recursivamente
                playNextSongRecursive(currentPlaylistIndex + 1);
            }
        }
    }
}

private void playNextSongRecursive(int index) {
    if (index < playlist.size()) {
        currentPlaylistIndex = index;
        currentSong = playlist.getItem(index);
        currentFrame = 0;
        currentTimeInMilli = 0;

        // Atualiza a interface gráfica
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);
        musicPlayerGUI.enablePauseButtonDisablePlayButton();

        // Toca a música
        playCurrentSong();
    }
}
// Outros atributos...
private List<Musica> favorites = new ArrayList<>(); // Lista de músicas favoritas

// Método para adicionar uma música aos favoritos
public void addToFavorites(Musica song) {
    if (!favorites.contains(song)) {
        favorites.add(song);
        System.out.println("Música adicionada aos favoritos: " + song.getMusicTitle());
    } else {
        System.out.println("Música já está nos favoritos!");
    }
}

// Método para remover uma música dos favoritos
public void removeFromFavorites(Musica song) {
    if (favorites.contains(song)) {
        favorites.remove(song);
        System.out.println("Música removida dos favoritos: " + song.getMusicTitle());
    } else {
        System.out.println("Música não está nos favoritos!");
    }
}

// Método para obter a lista de favoritos
public List<Musica> getFavorites() {
    return favorites;
}
}