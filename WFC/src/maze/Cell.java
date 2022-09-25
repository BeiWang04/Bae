package maze;

import processing.core.PApplet;
import wblut.geom.WB_Point;

import java.util.List;

public class Cell implements Comparable<Cell>{
    int index;//序号
    WB_Point loc;//位置
    //邻居们
    Cell up;
    Cell down;
    Cell left;
    Cell right;
    //是否已经坍缩
    public boolean collapsed;
    //选择范围
    public List<Integer> optis;
    //最后的选择
    public int pick=0;
    public int wallType;

    public Cell(WB_Point loc,boolean collapsed,List<Integer>optis){
        this.loc=loc;
        this.collapsed=collapsed;
        this.optis=optis;
    }
    public void setIndex(int id){this.index=id;}

    public void setUp(Cell up){
        this.up=up;
        up.down=this;
    }
    public void setDown(Cell down){
        this.down=down;
        down.up=this;
    }
    public void setLeft(Cell left){
        this.left=left;
        left.right=this;
    }
    public void setRight(Cell right){
        this.right=right;
        right.left=this;
    }


    public int compareTo(Cell o) {
        return this.optis.size() - o.optis.size();
    }
    public void draw(PApplet app){
        app.fill(0);
        //app.text(index,loc.xf()+50,loc.yf()+50);
        //if(collapsed){
        if(pick==0)app.fill(255);
        if(pick==2)app.fill(100,100);
        if(pick==1)app.fill(0);

            app.noStroke();
            app.rect(this.loc.xf(),this.loc.yf(),100,100);
            //app.image(Grid.tiles[pick],loc.xf(),loc.yf());
    //}
    }
}
