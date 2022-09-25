package maze;

import processing.core.PApplet;
import wblut.processing.WB_Render;

public class Render {
    PApplet app;
    WB_Render wb_render;
    //JtsRender jtsRender;
    public Render(PApplet home) {
        this.app = home;
        this.wb_render = new WB_Render(home);
        //this.jtsRender=new JtsRender(home);
    }

    public PApplet getApp() {
        return this.app;
    }

    public WB_Render getWB_Render(){
        return this.wb_render;
    }

//    public JtsRender getJtsRender() {
//        return this.jtsRender;
//    }
}
