import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
/*
compression
 * 1- read image
 *
 * 2- fill the decoded image with first row and col
 *  2.1 predict the whole decoded image
 *
 * 3- get the diff between the predicted image with the original
 *
 * 4- make the quitized difference
 *
 * 5- make the de-quiatized difference
 *
 * 6- constract the decoded image
 */


class TwoDPredictive{
    private Vector<int[]> qunatizer = new Vector<>();
    private int[][] image;
    private int[][] decodedImg;
    private int[][] quanDiff;
    private int imgWidth ;
    private int imgHeight;

    public TwoDPredictive(int step){
        // number of steps = 16.
        // full scale = 255.
        // step = full scale / number of steps = 16.
        double max = 255,  min = -255;
        int qStep = (int) Math.floor((max - min) / step);

        int start = -255, end = start + qStep;
        int[] range ;

        for(int i = 0; i < step; i++){
            range = new int[2];
            range[0] = start;
            range[1] = end;
            qunatizer.add(range);
            start = end + 1;
            end = start + qStep;
        }
    }

    public void compress(String imagString) throws IOException{
        readImg(imagString);
        decodedImg = new int[imgHeight][imgWidth];

        //row
        for (int i = 0; i < imgWidth; i++) {
            decodedImg[0][i] = image[0][i];
        }

        //col
        for (int i = 0; i < imgHeight; i++) {
            decodedImg[i][0] = image[i][0];
        }


        quanDiff = new int[imgHeight][imgWidth];

        for (int i = 1; i < imgHeight; i++) {
            for (int j = 1; j < imgWidth; j++) {
                int predicted , diff, quazDiff , deQuazDiff;
                if( decodedImg[i-1][j-1] <= Math.min(decodedImg[i-1][j], decodedImg[i][j-1]) ){
                    predicted = Math.max(decodedImg[i-1][j], decodedImg[i][j-1]);
                }else if (decodedImg[i-1][j-1] >= Math.max(decodedImg[i-1][j], decodedImg[i][j-1])){
                    predicted = Math.min(decodedImg[i-1][j], decodedImg[i][j-1]);
                }else{
                    predicted = decodedImg[i-1][j] + decodedImg[i][j-1] - decodedImg[i-1][j-1];
                }
                diff = image[i][j] - predicted;
                quazDiff = getQuantizerVal(diff);
                quanDiff[i][j] = quazDiff;
                deQuazDiff = (qunatizer.get(quazDiff)[0] + qunatizer.get(quazDiff)[1])/2;
                decodedImg[i][j] = predicted + deQuazDiff;
            }
        }
        writeToBinFile();
    }


    public int getQuantizerVal(int diff){
        for (int i = 0; i < qunatizer.size(); i++) {
            if (diff >= qunatizer.get(i)[0] && diff <= qunatizer.get(i)[1]  ){
                return i;
            }
        }

        if (diff > qunatizer.lastElement()[1]  ) {
            return qunatizer.size()-1;
        }else if( diff < qunatizer.firstElement()[0] ) {
            return 0;
        }

        return -1;
    }


    public void readImg(String filePath)  {
        // give image file as parameter to function and reads an image file and converts
        // its pixel values to a 2D array of floats
        File file = new File(filePath);
        BufferedImage img = null;
        // built in to read image

        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            System.out.println("vv");
        }

        // retrive width and height of image

        imgWidth = img.getWidth();
        imgHeight = img.getHeight();

        image = new int[imgHeight][imgWidth];
        for (int i = 0; i < imgWidth; i++) {
            for (int j = 0; j < imgHeight; j++) {
                int rgb = img.getRGB(i, j);
                int red = (rgb & 0x00ff0000) >> 16;
                int green = (rgb & 0x0000ff00) >> 8;
                int blue = (rgb & 0x000000ff);
                image[j][i] = Math.max(Math.max(red, green), blue);
                //imgPixels[i][j] = rgb;
            }
        }
    }


    public void writeToBinFile() throws IOException{
        //order of writting :
        /*
         * img Width
         * img height
         * first row  (as 1d array)
         * first col  (as 1d array)
         * qunatizer difference
         * qunatizer
         *
         *
         */
        FileOutputStream file = new FileOutputStream("compressed.bin");
        ObjectOutputStream out = new ObjectOutputStream(file);

        out.writeInt(imgWidth);
        out.writeInt(imgHeight);
        //row
        for (int i = 0; i < imgWidth; i++) {
            out.writeInt(image[0][i]);
        }

        //col
        for (int i = 0; i < imgHeight; i++) {
            out.writeInt(image[i][0]);
        }
        out.writeObject(quanDiff);
        out.writeObject(qunatizer);
    }

    
    public void writeImage(){
        BufferedImage theImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < imgHeight; i++) {
            for (int j = 0; j < imgWidth; j++) {
                int value = decodedImg[i][j] << 16 | decodedImg[i][j] << 8 | decodedImg[i][j];
                theImage.setRGB(j, i, value);
            }
        }

        File outputfile = new File("outputImg.jpg");
        try {
            ImageIO.write(theImage, "jpg", outputfile);
        } catch (IOException e1) {

        }
    }


    public void readBinFile() throws IOException, ClassNotFoundException {
        FileInputStream file = new FileInputStream("compressed.bin");
        ObjectInputStream in = new ObjectInputStream(file);

        imgWidth = in.readInt();
        imgHeight = in.readInt();
        decodedImg = new int[imgHeight][imgWidth];
        //row
        for (int i = 0; i < imgWidth; i++) {
            decodedImg[0][i] = in.readInt();
        }

        //col
        for (int i = 0; i < imgHeight; i++) {
            decodedImg[i][0] = in.readInt();
        }
        quanDiff = (int[][]) in.readObject();
        qunatizer = (Vector<int[]>) in.readObject();
    }


    public void decompress() throws IOException, ClassNotFoundException {
        readBinFile();

        for (int i = 1; i < imgHeight; i++) {
            for (int j = 1; j < imgWidth; j++) {
                int predicted ,deQuazDiff;
                if(decodedImg[i-1][j-1] <= Math.min(decodedImg[i-1][j], decodedImg[i][j-1]) ){
                    predicted = Math.max(decodedImg[i-1][j], decodedImg[i][j-1]);
                }else if (decodedImg[i-1][j-1] >= Math.max(decodedImg[i-1][j], decodedImg[i][j-1])){
                    predicted = Math.min(decodedImg[i-1][j], decodedImg[i][j-1]);
                }else{
                    predicted = decodedImg[i-1][j] + decodedImg[i][j-1] - decodedImg[i-1][j-1];
                }
                deQuazDiff = (qunatizer.get(quanDiff[i][j])[0] + qunatizer.get(quanDiff[i][j])[1])/2;
                decodedImg[i][j] = predicted + deQuazDiff;
                if(decodedImg[i][j] > 255){
                    decodedImg[i][j] = 255;
                }else if (decodedImg[i][j] < 0){
                    decodedImg[i][j] = 0;
                }
            }
        }
        writeImage();
    }
}