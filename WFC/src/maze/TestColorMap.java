package maze;


import edu.duke.geo4.colormapper.ColorMap;
import edu.duke.geo4.colormapper.DiscreteMap;
import edu.duke.geo4.colormapper.GradientMap;
import processing.core.PApplet;

import java.util.ArrayList;

/**
 * @ClassName: TestColorMap
 * @Description:
 * @author: zqy
 * @date: 2022/6/14/014 18:15
 */
public class TestColorMap extends PApplet {
    public static void main(String[] args) {PApplet.main("maze.TestColorMap");}
    int NUM_MAPS = 11;
    int bandHeight = 50;
    int fontHeight = 20;

    // Number of steps to use in the discrete colormaps
    int numSteps = 10;

    ArrayList<GradientMap> gradMaps;
    ArrayList<DiscreteMap> discMaps;
    ArrayList<ColorMap> maps;



    @Override
    public void setup(){
        size(800, bandHeight * NUM_MAPS);
        maps = new ArrayList<ColorMap>();
        maps.add(ColorMap.JET);
        maps.add(ColorMap.HSV);
        maps.add(ColorMap.HOT);
        maps.add(ColorMap.COOL);
        maps.add(ColorMap.SPRING);
        maps.add(ColorMap.SUMMER);
        maps.add(ColorMap.AUTUMN);
        maps.add(ColorMap.WINTER);
        maps.add(ColorMap.GRAYSCALE);
        maps.add(ColorMap.BONE);
        maps.add(ColorMap.COPPER);

        noLoop();
    }

    @Override
    public void draw(){
        background(255);
        for (int i = 0; i < maps.size(); i++) {
            drawGradBand(new GradientMap(this, maps.get(i)), i);
//            drawDiscBand(new DiscreteMap(this, maps.get(i), numSteps), i);
        }
    }


    /** Draws a band using a gradient colormap. */
    void drawGradBand(GradientMap map, int y) {
        for (int i = 0; i < width / 2; i++) {
            map.setMaxValue(width / 2);
            int col = map.getColor(i);
            System.out.println("MaxValue "+width/2);
            System.out.println("i: "+i);
            stroke(col);
            line(i, y * bandHeight, i, y * bandHeight + bandHeight);
        }

        // Label the band with the map name
        stroke(255);
        String mapString = map.toString();
        text(mapString.substring(11, mapString.length() - 2), 5, y * bandHeight + fontHeight);
    }

    /** Draws a band using a discrete colormap. */
    void drawDiscBand(DiscreteMap map, int y) {
        int stepWidth = (width / 2) / numSteps;
        for (int i = 0; i < width / 2; i++) {
            int col = map.getColor(i / stepWidth);
            stroke(col);
            line(i + width / 2, y * bandHeight, i + width / 2, y * bandHeight + bandHeight);
        }
    }
}
