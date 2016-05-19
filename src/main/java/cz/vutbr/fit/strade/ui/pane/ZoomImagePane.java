package cz.vutbr.fit.strade.ui.pane;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;

/**
 * Created by Lukas on 12/17/2015.
 */
public class ZoomImagePane extends ScrollPane{

    public static final Double ZOOM_FACTOR = 1.1;
    public static final int ZOOM_RATIO_X = 4;
    public static final int ZOOM_RATIO_Y = 3;

    private ImageView imageView;
    private final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

    public ZoomImagePane(){
        super();

        this.imageView = new ImageView();

        imageView.preserveRatioProperty().set(true);

        zoomProperty.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable arg0) {
                imageView.setFitWidth(zoomProperty.get() * ZOOM_RATIO_X);
                imageView.setFitHeight(zoomProperty.get() * ZOOM_RATIO_Y);
            }
        });

        this.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                if (event.getDeltaY() > 0) {
                    zoomProperty.set(zoomProperty.get() * ZOOM_FACTOR);
                } else if (event.getDeltaY() < 0) {
                    zoomProperty.set(zoomProperty.get() / ZOOM_FACTOR);
                }

            }
        });

        this.setContent(imageView);

    }

    public ImageView getImageView(){
        return this.imageView;
    }

    public void setImage(Image image){
        imageView.setImage(image);
        setContent(imageView);
    }

}
