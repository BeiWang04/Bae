package maze;

import peasy.PeasyCam;
import processing.core.PApplet;
import wblut.processing.WB_Render;

public class Test3D extends PApplet {
    public static void main(String[] args) {
        PApplet.main("maze.Test3D");
    }


    CubeMesh mesh;
    PeasyCam cam;
    WB_Render render;

    public void setup() {
        size(900, 900, P3D);
        cam = new PeasyCam(this, 500);
        mesh = new CubeMesh(20, 20, 20, 7f,20,this);
        render = new WB_Render(this);
    }

    public void draw() {
        background(255);
        mesh.draw(this, render);
        if (mesh.first) {
            mesh.firstCollapse();
        }
        if (mesh.second) {
            mesh.secondCollapse();
        }


    }

    public void keyReleased() {
        if(key=='A')mesh.first=!mesh.first;
        //mesh.firstCollapse();
    }
}
