import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SearchDialog extends JDialog {
    private JTextField searchField;
    private MusicPlayerGUI musicPlayerGUI;

    public SearchDialog(MusicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;

        setTitle("Buscar Música");
        setSize(300, 150);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(musicPlayerGUI);

        // Painel de Entrada
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        searchField = new JTextField(20);
        inputPanel.add(new JLabel("Nome da música:"));
        inputPanel.add(searchField);
        add(inputPanel, BorderLayout.CENTER);

        // Botões
        JPanel buttonPanel = new JPanel();
        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(searchButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void performSearch() {
        String songName = searchField.getText().trim();
        if (!songName.isEmpty()) {
            if (musicPlayerGUI.getMusicPlayer().getPlaylist() == null) {
                JOptionPane.showMessageDialog(this, "Nenhuma playlist carregada!");
                return;
            }

            // Buscar músicas na playlist
            List<Musica> resultados = musicPlayerGUI.getMusicPlayer().getPlaylist().buscarMusica(songName);
            if (!resultados.isEmpty()) {
                new SearchResultsDialog(musicPlayerGUI, resultados).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Nenhuma música encontrada!");
            }
            dispose();
        }
    }
}
