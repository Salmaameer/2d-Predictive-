import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GUI extends JFrame {
    JButton chooseBtn, compressBtn, decompressBtn;
    JLabel label;
    String imgPath;
    JTextArea noOfLevels;
    TwoDPredictive predictive;

    public GUI() {
        super("Vector Quantization");
        chooseBtn = new JButton("Choose Image");
        chooseBtn.setBounds(50, 300, 150, 40);
        compressBtn = new JButton("Compress");
        compressBtn.setBounds(420, 300, 100, 40);
        decompressBtn = new JButton("Decompress");
        decompressBtn.setBounds(540, 300, 110, 40);

        label = new JLabel();
        label.setBounds(10, 10, 670, 250);
        add(chooseBtn);
        add(compressBtn);
        add(decompressBtn);
        add(label);

        JLabel levelsLabel = new JLabel("Number of Levels:");
        levelsLabel.setBounds(450, 50, 120, 20);
        add(levelsLabel);

        noOfLevels = new JTextArea();
        noOfLevels.setBounds(565, 50, 50, 20);
        add(noOfLevels);

        chooseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser file = new JFileChooser();
                file.setCurrentDirectory(new File(System.getProperty("user.home")));
                // filter the files
                FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Images", "jpg", "png", "jpeg");
                file.addChoosableFileFilter(filter);

                int result = file.showSaveDialog(null); // to show file chooser dialog in center.

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = file.getSelectedFile();
                    imgPath = selectedFile.getAbsolutePath();
                    displayImage(imgPath);
                }
            }
        });

        compressBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int levelsNo = Integer.parseInt(noOfLevels.getText());
                    predictive = new TwoDPredictive(levelsNo);
                    predictive.compress(imgPath);
                    System.out.println("Image compressed successfully!");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        decompressBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    predictive.decompress();
                    displayImage("outputImg.jpg");
                } catch (IOException | ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        });

        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 400);
        setVisible(true);
    }

    public void displayImage(String imagePath) {
        ImageIcon imageIcon = new ImageIcon(imagePath);
        Image img = null;
        try {
            img = ImageIO.read(new File(imagePath));
            // Get the original image dimensions
            int imgWidth = img.getWidth(null);
            int imgHeight = img.getHeight(null);
            // Set label size to the original image dimensions
            label.setSize(imgWidth, imgHeight);
            // Scale the image to original dimensions
            Image scaledImg = img.getScaledInstance(imgWidth, imgHeight, Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(scaledImg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        label.setIcon(imageIcon);
    }

    public static void main(String[] args) {
        new GUI();
    }
}
