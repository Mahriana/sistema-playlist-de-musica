import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.util.LinkedList;
import java.util.Stack;

public class MusicPlayer extends PlaybackListener {
    private static final Object playSignal = new Object();

    private MusicPlayerGUI musicPlayerGUI;

    private Musica currentSong;
    public Musica getCurrentSong() {
        return currentSong;
    }

    // Usando a classe genérica Playlist<T> para armazenar músicas
    private Playlist<Musica> playlist;

    // Pilha para armazenar o histórico de músicas tocadas
    private Stack<Musica> historyStack;

    private int currentPlaylistIndex;

    private AdvancedPlayer advancedPlayer;

    private boolean isPaused;

    private boolean songFinished;

    private boolean pressedNext, pressedPrev;

    private int currentFrame;
    public void setCurrentFrame(int frame) {
        currentFrame = frame;
    }

    private int currentTimeInMilli;
    public void setCurrentTimeInMilli(int timeInMilli) {
        currentTimeInMilli = timeInMilli;
    }

    public MusicPlayer(MusicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
        this.historyStack = new Stack<>(); // Inicializa a pilha de histórico
    }

    public void loadSong(Musica song) {
        currentSong = song;
        playlist = null;

        if (!songFinished)
            stopSong();

        if (currentSong != null) {
            currentFrame = 0;
            currentTimeInMilli = 0;
            musicPlayerGUI.setPlaybackSliderValue(0);

            // Adiciona a música atual à pilha de histórico
            historyStack.push(currentSong);

            playCurrentSong();
        }
    }

    public void loadPlaylist(File playlistFile) {
        playlist = new Playlist<>(); // Inicializa a playlist genérica

        try {
            FileReader fileReader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String songPath;
            while ((songPath = bufferedReader.readLine()) != null) {
                Musica song = new Musica(songPath);
                playlist.addItem(song); // Adiciona músicas à playlist genérica
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (playlist.size() > 0) {
            playlist.ordenarPlaylist();
            musicPlayerGUI.setPlaybackSliderValue(0);
            currentTimeInMilli = 0;

            currentSong = playlist.getItem(0); // Obtém a primeira música da playlist
            currentFrame = 0;

            // Adiciona a música atual à pilha de histórico
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
        if (playlist == null) return;

        if (currentPlaylistIndex + 1 > playlist.size() - 1) return;

        pressedNext = true;

        if (!songFinished)
            stopSong();

        currentPlaylistIndex++;
        currentSong = playlist.getItem(currentPlaylistIndex); // Obtém a próxima música da playlist

        currentFrame = 0;
        currentTimeInMilli = 0;

        // Adiciona a música atual à pilha de histórico
        historyStack.push(currentSong);

        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);

        playCurrentSong();
    }

    public void prevSong() {
        if (playlist == null) return;

        if (currentPlaylistIndex - 1 < 0) return;

        pressedPrev = true;

        if (!songFinished)
            stopSong();

        currentPlaylistIndex--;
        currentSong = playlist.getItem(currentPlaylistIndex); // Obtém a música anterior da playlist

        currentFrame = 0;
        currentTimeInMilli = 0;

        // Adiciona a música atual à pilha de histórico
        historyStack.push(currentSong);

        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);

        playCurrentSong();
    }

    // Método para retroceder para a música anterior usando a pilha de histórico
    public void goBack() {
        if (!historyStack.isEmpty()) {
            Musica previousSong = historyStack.pop(); // Remove a música atual da pilha
            if (!historyStack.isEmpty()) {
                currentSong = historyStack.peek(); // Obtém a música anterior sem removê-la da pilha
                loadSong(currentSong); // Carrega a música anterior
            }
        }
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
        new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        }).start();
    }

    private void startPlaybackSliderThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                    musicPlayerGUI.enablePlayButtonDisablePauseButton();
                } else {
                    nextSong();
                }
            }
        }
    }
}

// Classe genérica para a playlist
class Playlist<T> {
    private LinkedList<T> items = new LinkedList<>();

    public void addItem(T item) {
        items.add(item);
    }

    public T getItem(int index) {
        return items.get(index);
    }

    public int size() {
        return items.size();
    }

    public Musica buscarMusica(String nome) {
        // Busca linear, caso a lista não esteja ordenada
        for (T item : items) {
            if (item instanceof Musica && ((Musica) item).getMusicTitle().equalsIgnoreCase(nome)) {
                return (Musica) item;
            }
        }
        return null; // Retorna null se a música não for encontrada
    }

    public Musica buscarMusicaBinaria(String nome) {
        // Busca binária (requer a lista ordenada)
        if (!(items.get(0) instanceof Musica)) return null;
    
        int esquerda = 0, direita = items.size() - 1;
        while (esquerda <= direita) {
            int meio = esquerda + (direita - esquerda) / 2;
            Musica musicaMeio = (Musica) items.get(meio);
    
            int comparacao = musicaMeio.getMusicTitle().compareToIgnoreCase(nome);
    
            if (comparacao == 0) {
                return musicaMeio;
            } else if (comparacao < 0) {
                esquerda = meio + 1;
            } else {
                direita = meio - 1;
            }
        }
        return null;
    }

    public void ordenarPlaylist() {
        if (items.isEmpty() || !(items.get(0) instanceof Musica)) return;
        
        items = mergeSort(items);
    }
    
    private LinkedList<T> mergeSort(LinkedList<T> lista) {
        if (lista.size() <= 1) return lista;
    
        int meio = lista.size() / 2;
        LinkedList<T> esquerda = new LinkedList<>(lista.subList(0, meio));
        LinkedList<T> direita = new LinkedList<>(lista.subList(meio, lista.size()));
    
        return merge(mergeSort(esquerda), mergeSort(direita));
    }
    
    private LinkedList<T> merge(LinkedList<T> esquerda, LinkedList<T> direita) {
        LinkedList<T> resultado = new LinkedList<>();
        int i = 0, j = 0;
    
        while (i < esquerda.size() && j < direita.size()) {
            Musica musicaEsq = (Musica) esquerda.get(i);
            Musica musicaDir = (Musica) direita.get(j);
    
            if (musicaEsq.getMusicTitle().compareToIgnoreCase(musicaDir.getMusicTitle()) < 0) {
                resultado.add(esquerda.get(i++));
            } else {
                resultado.add(direita.get(j++));
            }
        }
    
        while (i < esquerda.size()) resultado.add(esquerda.get(i++));
        while (j < direita.size()) resultado.add(direita.get(j++));
    
        return resultado;
    }
}