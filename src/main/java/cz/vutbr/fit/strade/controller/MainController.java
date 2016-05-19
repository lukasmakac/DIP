package cz.vutbr.fit.strade.controller;

import cz.vutbr.fit.strade.components.opticdisc.OpticDiscRecognizer;
import cz.vutbr.fit.strade.util.gui.GuiUtil;
import cz.vutbr.fit.strade.util.opencv.OpenCVUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.opencv.imgproc.Imgproc.cvtColor;

public class MainController {

    private final static Logger LOGGER = Logger.getLogger("MainController");
    private static final String DEFAULT_DIR = "c:\\Users\\Lukas\\Google Drive\\FIT\\1DVI\\DIZ\\resources\\Sitnice\\";

    public static final Double ZOOM_FACTOR = 1.1;
    public static final int ZOOM_RATIO_X = 4;
    public static final int ZOOM_RATIO_Y = 3;

    private final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

    @FXML
    private Slider zoom;

    @FXML
    private MenuItem menutItemDetectDiseases;

    @FXML
    private ScrollPane originalScrollPane;
    @FXML
    private ScrollPane convertedScrollPane;
    @FXML
    private ImageView originalImageView;
    @FXML
    private ImageView convertedImageView;

    @FXML
    private ScrollPane processedImagesScrollPane;

    @FXML
    private VBox processedImagesContainer;

    private FileChooser imageChooser;
    private Mat sourceImage;


    public MainController() {
        this.imageChooser = new FileChooser();
        this.imageChooser.setInitialDirectory(new File("c:\\Users\\Lukas\\FIT\\1DVI\\DIZ\\resources\\Sitnice\\"));
    }

    @FXML
    private void initialize(){
        originalImageView.fitWidthProperty().bind(originalScrollPane.widthProperty());
        convertedImageView.fitWidthProperty().bind(convertedScrollPane.widthProperty());

//        initZoom(originalScrollPane, originalImageView);
//        initZoom(convertedScrollPane, convertedImageView);
    }

    protected void initZoom(ScrollPane scrollPane, final ImageView imageView){
        zoomProperty.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable arg0) {
                imageView.setFitWidth(zoomProperty.get() * ZOOM_RATIO_X);
                imageView.setFitHeight(zoomProperty.get() * ZOOM_RATIO_Y);
            }
        });

        scrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                if (event.getDeltaY() > 0) {
                    zoomProperty.set(zoomProperty.get() * ZOOM_FACTOR);
                } else if (event.getDeltaY() < 0) {
                    zoomProperty.set(zoomProperty.get() / ZOOM_FACTOR);
                }

            }
        });
    }

    @FXML
    protected void onOpenImageAction(ActionEvent event) {
        //remove old pics
        this.originalImageView.setImage(null);
        this.convertedImageView.setImage(null);
        ((VBox) this.processedImagesContainer).getChildren().clear();


        //load image into pane
        File image = imageChooser.showOpenDialog(null);

        if(image != null){
            try{
                BufferedImage bufferedImage = ImageIO.read(image);
                Image imageFX = SwingFXUtils.toFXImage(bufferedImage, null);
                this.originalImageView.setImage(imageFX);

                this.sourceImage = Imgcodecs.imread(image.getPath(), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);

            }catch(IOException ioe){
                System.err.println(String.format("Error while loading image: {0}", ioe));
                LOGGER.log(Level.SEVERE, "Error while loading image", ioe);
            }
        }
    }

    @FXML
    protected void onConvertAction(ActionEvent event){
        Mat grayImage = this.sourceImage.clone();

        //processed images
        Mat redImage = OpenCVUtil.getChannel(this.sourceImage, OpenCVUtil.CHANNEL_RED);
        Mat greenImage = OpenCVUtil.getChannel(this.sourceImage, OpenCVUtil.CHANNEL_GREEN);
        Mat blueImage = OpenCVUtil.getChannel(this.sourceImage, OpenCVUtil.CHANNEL_BLUE);

        //obtain gray image
        cvtColor(this.sourceImage, grayImage, Imgproc.COLOR_RGB2GRAY);

        //create Recognizer
        OpticDiscRecognizer opticDiscRecognizer = new OpticDiscRecognizer(greenImage, redImage, grayImage);

        //provide segmentation
        Mat segmentedImage = opticDiscRecognizer.segmentation(OpenCVUtil.convertToGray(greenImage));

        //add results to pane
        GuiUtil.addProcessedImage(processedImagesScrollPane, processedImagesContainer, redImage);
        GuiUtil.addProcessedImage(processedImagesScrollPane, processedImagesContainer, greenImage);
        GuiUtil.addProcessedImage(processedImagesScrollPane, processedImagesContainer, blueImage);

        //add result
        GuiUtil.addProcessedImage(this.convertedImageView, segmentedImage);

    }

    @FXML
    protected void onDetectDiseasesAction(ActionEvent event){

    }




}
