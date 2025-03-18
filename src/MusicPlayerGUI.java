import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;
import java.util.List;

public class MusicPlayerGUI extends JFrame {
    public static final Color FRAME_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;

    private MusicPlayer musicPlayer;
    private JFileChooser jFileChooser;
    private JLabel songTitle, songArtist;
    private JPanel playbackBtns;
    private JSlider playbackSlider;

    public MusicPlayerGUI() {
        super("Player de Música");
        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer(this);
        jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new File("src/assets"));
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));

        addGuiComponents();
    }

    private void addGuiComponents() {
        addToolbar();

        JLabel songImage = new JLabel(loadImage("src/assets/record.png"));
        songImage.setBounds(0, 50, getWidth() - 20, 225);
        add(songImage);

        songTitle = new JLabel("Título da Música");
        songTitle.setBounds(0, 285, getWidth() - 10, 30);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(songTitle);

        songArtist = new JLabel("Artista");
        songArtist.setBounds(0, 315, getWidth() - 10, 30);
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 24));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setHorizontalAlignment(SwingConstants.CENTER);
        add(songArtist);

        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth() / 2 - 300 / 2, 365, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                musicPlayer.pauseSong(); // Pausa a música enquanto o usuário arrasta a barra
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                JSlider source = (JSlider) e.getSource();
                int frame = source.getValue(); // Obtém o valor do slider

                // Define o frame atual da música com base no valor do slider
                musicPlayer.setCurrentFrame(frame);

                // Calcula o tempo atual em milissegundos
                int timeInMilli = (int) (frame / (2.08 * musicPlayer.getCurrentSong().getFrameRatePerMilliseconds()));
                musicPlayer.setCurrentTimeInMilli(timeInMilli);

                // Continua a reprodução da música a partir do frame selecionado
                musicPlayer.playCurrentSong();
                enablePauseButtonDisablePlayButton();
            }
        });
        add(playbackSlider);

        addPlaybackBtns();
    }

    private void addToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);
        toolBar.setFloatable(false);

        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        JMenu songMenu = new JMenu("Música");
        menuBar.add(songMenu);

        JMenuItem loadSong = new JMenuItem("Carregar Música");
        loadSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    Musica song = new Musica(selectedFile.getPath());
                    loadSong(song);
                }
            }
        });
        songMenu.add(loadSong);

        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        JMenuItem createPlaylist = new JMenuItem("Criar Playlist");
        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new MusicPlaylistDialog(MusicPlayerGUI.this).setVisible(true);
            }
        });
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Carregar Playlist");
        loadPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist", "txt"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));

                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    musicPlayer.stopSong();
                    musicPlayer.loadPlaylist(selectedFile);
                }
            }
        });
        playlistMenu.add(loadPlaylist);

        JMenu searchSortMenu = new JMenu("Buscar/Ordenar");
        menuBar.add(searchSortMenu);

        JMenuItem searchSong = new JMenuItem("Buscar Música");
        searchSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String songName = JOptionPane.showInputDialog(
                    MusicPlayerGUI.this,
                    "Digite o nome da música:",
                    "Buscar Música",
                    JOptionPane.PLAIN_MESSAGE
                );

                if (songName != null && !songName.trim().isEmpty()) {
                    List<Musica> resultados = musicPlayer.getPlaylist().buscarMusica(songName.trim());
                    if (!resultados.isEmpty()) {
                        new SearchResultsDialog(MusicPlayerGUI.this, resultados).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(
                            MusicPlayerGUI.this,
                            "Nenhuma música encontrada!",
                            "Resultado da Busca",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } else if (songName != null) {
                    JOptionPane.showMessageDialog(
                        MusicPlayerGUI.this,
                        "Por favor, digite o nome da música.",
                        "Entrada Inválida",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });
        searchSortMenu.add(searchSong);

        JMenuItem sortByTitle = new JMenuItem("Ordenar por Título");
        sortByTitle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.getPlaylist().ordenarPorTitulo();
                JOptionPane.showMessageDialog(MusicPlayerGUI.this, "Playlist ordenada por título!");
            }
        });
        searchSortMenu.add(sortByTitle);

        JMenuItem sortByArtist = new JMenuItem("Ordenar por Artista");
        sortByArtist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.getPlaylist().ordenarPorArtista();
                JOptionPane.showMessageDialog(MusicPlayerGUI.this, "Playlist ordenada por artista!");
            }
        });
        searchSortMenu.add(sortByArtist);

        JMenuItem sortByDuration = new JMenuItem("Ordenar por Duração");
        sortByDuration.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.getPlaylist().ordenarPorDuracao();
                JOptionPane.showMessageDialog(MusicPlayerGUI.this, "Playlist ordenada por duração!");
            }
        });
        searchSortMenu.add(sortByDuration);

        JMenu favoritesMenu = new JMenu("Favoritos");
        menuBar.add(favoritesMenu);

        JMenuItem toggleFavorite = new JMenuItem("Adicionar/Remover Favorito");
        toggleFavorite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Musica currentSong = musicPlayer.getCurrentSong();
                if (currentSong != null) {
                    if (musicPlayer.getFavorites().contains(currentSong)) {
                        musicPlayer.removeFromFavorites(currentSong);
                    } else {
                        musicPlayer.addToFavorites(currentSong);
                    }
                } else {
                    JOptionPane.showMessageDialog(MusicPlayerGUI.this, "Nenhuma música está tocando!", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        favoritesMenu.add(toggleFavorite);

        JMenuItem showFavorites = new JMenuItem("Exibir Favoritos");
        showFavorites.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Musica> favorites = musicPlayer.getFavorites();
                if (!favorites.isEmpty()) {
                    new SearchResultsDialog(MusicPlayerGUI.this, favorites).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(MusicPlayerGUI.this, "Nenhuma música favorita encontrada!", "Favoritos", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        favoritesMenu.add(showFavorites);

        add(toolBar);
    }

    private void addPlaybackBtns() {
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 435, getWidth() - 10, 80);
        playbackBtns.setBackground(null);

        JButton prevButton = new JButton(loadImage("src/assets/previous.png"));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.prevSong();
            }
        });
        playbackBtns.add(prevButton);

        JButton playButton = new JButton(loadImage("src/assets/play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePauseButtonDisablePlayButton();
                musicPlayer.playCurrentSong();
            }
        });
        playbackBtns.add(playButton);

        JButton pauseButton = new JButton(loadImage("src/assets/pause.png"));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePlayButtonDisablePauseButton();
                musicPlayer.pauseSong();
            }
        });
        playbackBtns.add(pauseButton);

        JButton nextButton = new JButton(loadImage("src/assets/next.png"));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.nextSong();
            }
        });
        playbackBtns.add(nextButton);

        add(playbackBtns);
    }

    public void setPlaybackSliderValue(int frame) {
        try {
            playbackSlider.setValue(frame);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao atualizar a barra de tempo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateSongTitleAndArtist(Musica song) {
        songTitle.setText(song.getMusicTitle());
        songArtist.setText(song.getMusicArtist());
    }

    public void updatePlaybackSlider(Musica song) {
        playbackSlider.setMaximum(song.getMp3File().getFrameCount());

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        JLabel labelBeginning = new JLabel("00:00");
        labelBeginning.setFont(new Font("Dialog", Font.BOLD, 18));
        labelBeginning.setForeground(TEXT_COLOR);

        JLabel labelEnd = new JLabel(song.getMusicLength());
        labelEnd.setFont(new Font("Dialog", Font.BOLD, 18));
        labelEnd.setForeground(TEXT_COLOR);

        labelTable.put(0, labelBeginning);
        labelTable.put(song.getMp3File().getFrameCount(), labelEnd);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);
    }

    public void enablePauseButtonDisablePlayButton() {
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        playButton.setVisible(false);
        playButton.setEnabled(false);

        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);
    }

    public void enablePlayButtonDisablePauseButton() {
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        playButton.setVisible(true);
        playButton.setEnabled(true);

        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
    }

    private ImageIcon loadImage(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            return new ImageIcon(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadSong(Musica song) {
        try {
            musicPlayer.loadSong(song);
            updateSongTitleAndArtist(song);
            updatePlaybackSlider(song);
            enablePauseButtonDisablePlayButton();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar a música: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}