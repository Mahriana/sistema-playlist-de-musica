import java.util.LinkedList;
import java.util.List;

// LINKEDLIST E GENERIC
public class Playlist<T> {
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

    // BUSCA LINEAR
    public List<Musica> buscarMusica(String nome) {
        // LINKEDLIST
        List<Musica> resultados = new LinkedList<>();
        for (T item : items) {
            if (item instanceof Musica) {
                Musica musica = (Musica) item;
                if (musica.getMusicTitle().toLowerCase().contains(nome.toLowerCase())) {
                    resultados.add(musica);
                }
            }
        }
        return resultados;
    }

    public void ordenarPorTitulo() {
        items.sort((item1, item2) -> ((Musica) item1).getMusicTitle().compareToIgnoreCase(((Musica) item2).getMusicTitle()));
    }

    public void ordenarPorArtista() {
        items.sort((item1, item2) -> ((Musica) item1).getMusicArtist().compareToIgnoreCase(((Musica) item2).getMusicArtist()));
    }

    public void ordenarPorDuracao() {
        insertionSort();
    }

    // ORDENAÇÃO
    private void insertionSort() {
        int n = items.size();
        for (int i = 1; i < n; i++) {
            T key = items.get(i);
            long keyDuration = ((Musica) key).getMp3File().getLengthInSeconds();
            int j = i - 1;

            // Move elementos maiores que a duração atual uma posição à frente
            while (j >= 0 && ((Musica) items.get(j)).getMp3File().getLengthInSeconds() > keyDuration) {
                items.set(j + 1, items.get(j));
                j--;
            }
            items.set(j + 1, key);
        }
    }


}