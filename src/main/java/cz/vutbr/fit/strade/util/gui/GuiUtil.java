package cz.vutbr.fit.strade.util.gui;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Pair;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Created by lukas.maruniak on 8.5.2015.
 */
public class GuiUtil {

    public static void displayImage(String name, Mat image, Pane pane){

    }

    public static BufferedImage toBufferedImage(Mat m){
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;

    }

    public static void showAlert(String headerText, String content){

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(headerText);
        alert.setContentText(content);

        alert.showAndWait();
    }

    public static void showError(String headerText, String content){

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(content);

        alert.showAndWait();
    }

    public static void addProcessedImage(Pane pane, Mat image){
        //convert to displayable image
        Image I = SwingFXUtils.toFXImage(GuiUtil.toBufferedImage(image), null);

        //display Image
        ImageView imageView = new ImageView();
        imageView.setImage(I);

        //set proper height and width
        imageView.fitWidthProperty().bind(pane.widthProperty());
        //imageView.fitHeightProperty().bind(pane.heightProperty());
        imageView.setPreserveRatio(true);

        pane.getChildren().add(imageView);
    }

    public static void addProcessedImage(ScrollPane root, Pane pane, Mat image){
        //convert to displayable image
        Image I = SwingFXUtils.toFXImage(GuiUtil.toBufferedImage(image), null);

        //display Image
        ImageView imageView = new ImageView();
        imageView.setImage(I);

        //set proper height and width
        imageView.fitWidthProperty().bind(root.widthProperty());
        //imageView.fitHeightProperty().bind(pane.heightProperty());
        imageView.setPreserveRatio(true);

        pane.getChildren().add(imageView);
    }

    public static void addProcessedImage(ScrollPane pane, Mat image){
        //convert to displayable image
        Image I = SwingFXUtils.toFXImage(GuiUtil.toBufferedImage(image), null);

        //display Image
        ImageView imageView = new ImageView();
        imageView.setImage(I);

        //set proper height and width
        imageView.fitWidthProperty().bind(pane.widthProperty());
        //imageView.fitHeightProperty().bind(pane.heightProperty());
        imageView.setPreserveRatio(true);

        pane.setContent(imageView);
    }

    public static void addProcessedImage(ImageView imageView, Mat input){
        Image i = SwingFXUtils.toFXImage(GuiUtil.toBufferedImage(input), null);

        imageView.setImage(i);
        imageView.setPreserveRatio(true);
    }

    public static void toConsole(Pair<?,?> result, TextArea consoleOutput){
        //display statistics
        consoleOutput.appendText("\n--------------------------- Result ---------------------------\n");
        consoleOutput.appendText(String.valueOf(result.getKey()).concat("\n"));
        consoleOutput.appendText(String.valueOf(result.getValue()));
    }

    public static void addSeriesToChart(LineChart chart, XYChart.Series series, String name){
        series.setName(name);
        chart.getData().addAll(series);
    }

}
