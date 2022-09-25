package maze;

import peasy.PeasyCam;
import processing.core.PApplet;
import wblut.processing.WB_Render;

public class Main extends PApplet {

    WB_Render render;
    PeasyCam cam;
    //CameraController cam;

    Grid grid = new Grid(19, 19, this);

    public static void main(String[] args) {
        PApplet.main("maze.Main");
    }

    public void setup() {
        size(1000, 1000, P3D);
        this.render=new WB_Render(this);
        cam=new PeasyCam(this,200);
        //cam=new CameraController(this);
    }

    public void draw() {
        background(255);
        if(grid.first){grid.firstCollapse();}
        if (grid.second){grid.secondCollapse();}
        grid.draw(this,render);

    }
    public void keyReleased(){
        if(key=='A')grid.first=!grid.first;
    }

}
