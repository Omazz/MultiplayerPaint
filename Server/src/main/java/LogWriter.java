import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogWriter {
    private static final String filepath = "log.log";
    private static FileWriter fileWriter;

    static {
        try {
            fileWriter = new FileWriter(filepath);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static boolean writeEvent(String event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss");
        try {
            fileWriter.write(event + LocalDateTime.now().format(formatter) + "\n\n");
            fileWriter.flush();
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }
}
