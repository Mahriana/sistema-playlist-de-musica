import java.util.LinkedList;
import java.util.List;

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

    public List<Musica> buscarMusica(String nome) {
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
        items.sort((item1, item2) -> Long.compare(((Musica) item1).getMp3File().getLengthInSeconds(), ((Musica) item2).getMp3File().getLengthInSeconds()));
    }
}