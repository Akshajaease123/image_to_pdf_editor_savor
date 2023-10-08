import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ImageImportApp extends JFrame {
    private JButton importButton;
    private JButton saveButton;
    private JLabel statusLabel;
    private JLabel imageLabel;
    private JFileChooser fileChooser;
    private JSlider brightnessSlider;
    private JComboBox<String> filterComboBox;
    private JRadioButton saveAsImageRadioButton;
    private JRadioButton saveAsPDFRadioButton;

    private BufferedImage originalImage;
    private BufferedImage displayedImage;

    public ImageImportApp() {
        setTitle("Image Importer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create components
        importButton = new JButton("Import Image");
        saveButton = new JButton("Save");
        statusLabel = new JLabel("Status: ");
        imageLabel = new JLabel();
        fileChooser = new JFileChooser();
        brightnessSlider = new JSlider(-100, 100, 0);
        filterComboBox = new JComboBox<>(new String[]{"Original", "Black and White", "Sepia"});
        saveAsImageRadioButton = new JRadioButton("Save as Image");
        saveAsPDFRadioButton = new JRadioButton("Save as PDF");

        // Create a button group for the radio buttons
        ButtonGroup saveFormatGroup = new ButtonGroup();
        saveFormatGroup.add(saveAsImageRadioButton);
        saveFormatGroup.add(saveAsPDFRadioButton);

        // Add components to the frame
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(importButton);
        topPanel.add(saveButton);
        topPanel.add(statusLabel);
        topPanel.add(filterComboBox);
        topPanel.add(saveAsImageRadioButton);
        topPanel.add(saveAsPDFRadioButton);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(imageLabel, BorderLayout.CENTER);
        centerPanel.add(brightnessSlider, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Add action listener to the import button
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String imagePath = selectedFile.getAbsolutePath();

                    // Define the destination folder in your project (e.g., "images")
                    String destinationFolder = "images";

                    try {
                        // Copy the selected image to the destination folder
                        File destFile = new File(destinationFolder + File.separator + selectedFile.getName());
                        Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                        // Load the imported image
                        originalImage = ImageIO.read(destFile);
                        displayedImage = originalImage;

                        // Display the imported image
                        updateImageLabel();
                        statusLabel.setText("Status: Image imported successfully to " + destinationFolder);
                    } catch (IOException ex) {
                        statusLabel.setText("Status: Error importing image");
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Add action listener to the save button
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (originalImage != null) {
                    if (saveAsImageRadioButton.isSelected()) {
                        saveEditedImageAsImage();
                    } else if (saveAsPDFRadioButton.isSelected()) {
                        saveEditedImageAsPDF();
                    }
                }
            }
        });

        // Add change listener to the brightness slider
        brightnessSlider.addChangeListener(e -> {
            if (originalImage != null) {
                int brightnessValue = brightnessSlider.getValue();
                adjustBrightness(brightnessValue);
                updateImageLabel();
            }
        });

        // Add action listener to the filter combo box
        filterComboBox.addActionListener(e -> {
            if (originalImage != null) {
                applyFilter(filterComboBox.getSelectedItem().toString());
                updateImageLabel();
            }
        });
    }

    private void adjustBrightness(int brightnessValue) {
        float scaleFactor = (float) (1 + brightnessValue / 100.0);
        RescaleOp rescaleOp = new RescaleOp(scaleFactor, 0, null);
        displayedImage = rescaleOp.filter(originalImage, null);
    }

    private void applyFilter(String filterName) {
        BufferedImageOp filter = null;

        switch (filterName) {
            case "Black and White":
                filter = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
                break;
            case "Sepia":
                BufferedImage sepiaImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

    for (int y = 0; y < originalImage.getHeight(); y++) {
        for (int x = 0; x < originalImage.getWidth(); x++) {
            int rgb = originalImage.getRGB(x, y);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            int tr = (int) (0.393 * r + 0.769 * g + 0.189 * b);
            int tg = (int) (0.349 * r + 0.686 * g + 0.168 * b);
            int tb = (int) (0.272 * r + 0.534 * g + 0.131 * b);

            // Clamp values to the 0-255 range
            tr = Math.min(255, Math.max(0, tr));
            tg = Math.min(255, Math.max(0, tg));
            tb = Math.min(255, Math.max(0, tb));

            int sepiaRGB = (tr << 16) | (tg << 8) | tb;
            sepiaImage.setRGB(x, y, sepiaRGB);
        }
    }

    displayedImage = sepiaImage;
                break;
            case "Original":
                // No filter needed for original
                break;
        }

        if (filter != null) {
            BufferedImage filteredImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
            filter.filter(originalImage, filteredImage);
            displayedImage = filteredImage;
        }
    }

    private BufferedImageOp createSepiaFilter() {
        float[] sepiaFactors = {0.393f, 0.769f, 0.189f, 0, 0, 0.349f, 0.686f, 0.168f, 0, 0, 0.272f, 0.534f, 0.131f, 0, 0, 0, 0, 0, 1, 0};

        RescaleOp rescaleOp = new RescaleOp(sepiaFactors, new float[4], null);
        return rescaleOp;
    }

    private void updateImageLabel() {
        ImageIcon imageIcon = new ImageIcon(displayedImage);
        imageLabel.setIcon(imageIcon);
        imageLabel.setText(""); // Clear text
    }

    // Method to save the edited image as an image
    private void saveEditedImageAsImage() {
        if (displayedImage != null) {
            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "gif"));
            int returnValue = saveFileChooser.showSaveDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = saveFileChooser.getSelectedFile();
                String savePath = selectedFile.getAbsolutePath();

                // Ensure the image has the correct file extension
                String fileExtension = getFileExtension(savePath);
                if (!fileExtension.isEmpty() && !savePath.endsWith("." + fileExtension)) {
                    savePath += "." + fileExtension;
                }

                try {
                    ImageIO.write(displayedImage, fileExtension, new File(savePath));
                    statusLabel.setText("Status: Edited image saved successfully as " + savePath);
                } catch (IOException ex) {
                    statusLabel.setText("Status: Error saving edited image");
                    ex.printStackTrace();
                }
            }
        }
    }

    // Method to save the edited image as a PDF file
    private void saveEditedImageAsPDF() {
        if (displayedImage != null) {
            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
            int returnValue = saveFileChooser.showSaveDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = saveFileChooser.getSelectedFile();
                String savePath = selectedFile.getAbsolutePath();

                // Ensure the file has the correct file extension
                if (!savePath.toLowerCase().endsWith(".pdf")) {
                    savePath += ".pdf";
                }

                try {
                    PDDocument document = new PDDocument();
                    PDPage page = new PDPage(new PDRectangle(displayedImage.getWidth(), displayedImage.getHeight()));
                    document.addPage(page);

                    // Convert BufferedImage to PDImageXObject
                    PDImageXObject pdImage = LosslessFactory.createFromImage(document, displayedImage);

                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                        contentStream.drawImage(pdImage, 0, 0);
                    }

                    document.save(savePath);
                    document.close();
                    statusLabel.setText("Status: Edited image saved as PDF successfully as " + savePath);
                } catch (IOException ex) {
                    statusLabel.setText("Status: Error saving edited image as PDF");
                    ex.printStackTrace();
                }
            }
        }
    }

    // Helper method to get file extension
    private String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1);
        }
        return "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ImageImportApp app = new ImageImportApp();
                app.setVisible(true);
            }
        });
    }
}
