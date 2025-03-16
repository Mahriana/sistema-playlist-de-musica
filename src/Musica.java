import com.mpatric.mp3agic.Mp3File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

public class Musica {
    private String musicTitle;
    private String musicArtist;
    private String musicLength;
    private String filePath;
    private Mp3File mp3File;
    private double frameRatePerMilliseconds;

    // Construtor
    public Musica(String filePath){
        this.filePath = filePath;
        try{
            mp3File = new Mp3File(filePath);
            frameRatePerMilliseconds = (double) mp3File.getFrameCount() / mp3File.getLengthInMilliseconds();
            musicLength = convertToSongLengthFormat();

            // Cria um objeto audiofile para ler mp3
            AudioFile audioFile = AudioFileIO.read(new File(filePath));

            // lê os metadados do arquivo de áudio
            Tag tag =  audioFile.getTag();
            if(tag != null){
                musicTitle = tag.getFirst(FieldKey.TITLE);
                musicArtist = tag.getFirst(FieldKey.ARTIST);
            }else{
                // Caso não consiga ler
                musicTitle = "N/A";
                musicArtist = "N/A";
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // Tempo de música
    private String convertToSongLengthFormat(){
        long minutes = mp3File.getLengthInSeconds() / 60;
        long seconds = mp3File.getLengthInSeconds() % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);

        return formattedTime;
    }

    // getters
    public String getMusicTitle() {
        return musicTitle;
    }

    public String getMusicArtist() {
        return musicArtist;
    }

    public String getMusicLength() {
        return musicLength;
    }

    public String getFilePath() {
        return filePath;
    }

    public Mp3File getMp3File(){
        return mp3File;
    }

    public double getFrameRatePerMilliseconds(){
        return frameRatePerMilliseconds;
    }
}