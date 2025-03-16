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