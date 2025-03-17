import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SearchResultsDialog extends JDialog {
    private MusicPlayerGUI musicPlayerGUI;
    private List<Musica> searchResults;

    public SearchResultsDialog(MusicPlayerGUI musicPlayerGUI, List<Musica> searchResults) {
        this.musicPlayerGUI = musicPlayerGUI;
        this.searchResults = searchResults;

        setTitle("Resultados da Busca");
        setSize(400, 300);
        setResizable(false);
        getContentPane().setBackground(MusicPlayerGUI.FRAME_COLOR);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(musicPlayerGUI);

        addDialogComponents();
    }

    private void addDialogComponents() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Musica song : searchResults) {
            listModel.addElement(song.getMusicTitle() + " - " + song.getMusicArtist());
        }

        JList<String> resultsList = new JList<>(listModel);
        resultsList.setFont(new Font("Dialog", Font.PLAIN, 14));
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(resultsList);
        add(scrollPane, BorderLayout.CENTER);

        JButton loadButton = new JButton("Carregar Música");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = resultsList.getSelectedIndex();
                if (selectedIndex != -1) {
                    Musica selectedSong = searchResults.get(selectedIndex);
                    musicPlayerGUI.loadSong(selectedSong);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(SearchResultsDialog.this, "Selecione uma música!");
                }
            }
        });
        add(loadButton, BorderLayout.SOUTH);
    }
}