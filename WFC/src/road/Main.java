package road;

import processing.core.PApplet;

public class Main extends PApplet {
    Grid grid=new Grid(10,10,this);

    public static void main(String[] args) {
        PApplet.main("road.Main");
    }
    public void setup(){
        size(1000,1000);
    }
    public void draw(){

        background(255);
        grid.collapse();
        grid.draw(this);

    }
    public void mouseReleased(){
        saveFrame("F:\\Learn\\研二\\flo-####.png");
    }
}
