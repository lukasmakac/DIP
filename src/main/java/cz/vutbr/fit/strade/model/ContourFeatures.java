package cz.vutbr.fit.strade.model;

import org.opencv.core.Mat;

/**
 * Structure that represents features of contour/element.
 */
public class ContourFeatures {
    
    private double area;
    private double compactness;
    private double avgBoundaryIntensity;
    private double minBoundaryIntensity;
    private double maxBoundaryIntensity;
    private double meanHue;
    private double meanSaturation;
    private double meanIntensity;
    private double meanGradientMagnitude;
    private double energy;
    private double entropy;
    private double ratio;

    public ContourFeatures(double area, double compactness, double avgBoundaryIntensity,
                           double minBoundaryIntensity, double maxBoundaryIntensity,
                           double meanHue, double meanSaturation, double meanIntensity,
                           double meanGradientMagnitude, double energy, double entropy, double ratio) {
        this.area = area;
        this.compactness = compactness;
        this.avgBoundaryIntensity = avgBoundaryIntensity;
        this.minBoundaryIntensity = minBoundaryIntensity;
        this.maxBoundaryIntensity = maxBoundaryIntensity;
        this.meanHue = meanHue;
        this.meanSaturation = meanSaturation;
        this.meanIntensity = meanIntensity;
        this.meanGradientMagnitude = meanGradientMagnitude;
        this.energy = energy;
        this.entropy = entropy;
        this.ratio = ratio;
    }

    public double[] asDoubleData(){
        return new double[]{
                area,
                compactness,
                avgBoundaryIntensity,
                minBoundaryIntensity,
                maxBoundaryIntensity,
                meanHue,
                meanSaturation,
                meanIntensity,
                meanGradientMagnitude,
                energy,
                entropy,
                ratio
        };
    }

}
