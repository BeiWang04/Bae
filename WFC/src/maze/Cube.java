package maze;

import processing.core.PApplet;
import wblut.geom.WB_Point;

import java.util.List;

public class Cube{
    int index;//序号
    int size;
    WB_Point loc;//位置
    //邻居们
    Cube up;
    Cube down;
    Cube left;
    Cube right;
    Cube front;
    Cube behind;

    //是否已经坍缩
    public boolean collapsed;
    public boolean inner;
    public boolean boundary;
    public boolean corner;
    public int type;
    //选择范围
    public List<Integer> optis;
    //最后的选择
    public int pick=0;
    public int wallType;

    public Cube(WB_Point loc,boolean collapsed,List<Integer>optis,int size){
        this.loc=loc;
        this.collapsed=collapsed;
        this.optis=optis;
        this.inner=false;
        this.boundary=false;
        this.corner=false;
        this.type=-1;
        this.size=size;
    }
    public void setIndex(int id){this.index=id;}

    public void setUp(Cube up){
        this.up=up;
        up.down=this;
    }
    public void setDown(Cube down){
        this.down=down;
        down.up=this;
    }
    public void setLeft(Cube left){
        this.left=left;
        left.right=this;
    }
    public void setRight(Cube right){
        this.right=right;
        right.left=this;
    }
    public void setFront(Cube front){
        this.front=front;
        front.behind=this;
    }
    public void setBehind(Cube behind){
        this.behind=behind;
        behind.front=this;
    }


    public void draw(PApplet app){
        if(!inner) {

            if (pick == 0) app.fill(255,100);
            if (pick == 2) app.fill(100, 100);
            if (pick == 1) app.fill(0,100);


    }else{
           app.fill(255,2);
        }
        app.pushMatrix();
        app.noStroke();
        app.translate(loc.xf(),loc.yf(),loc.zf());
        app.box(size);
        app.popMatrix();

    }
}
