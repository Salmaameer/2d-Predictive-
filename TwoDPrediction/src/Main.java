import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        TwoDPredictive p = new TwoDPredictive(16);
        p.compress("House.BMP");
        p.decompress();
    }
}