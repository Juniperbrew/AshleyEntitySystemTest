package util;

import core.Global;
import tiled.core.Tile;
import tiled.core.TileLayer;

import java.util.Vector;

/**
 * Created by Juniperbrew on 14.1.2015.
 */
public class MapMask {

    public boolean[][] mask;
    public int height;
    public int width;

    public MapMask(int height, int width, Vector<TileLayer> layers, String propertyKey) {
        this.height = height;
        this.width = width;

        mask = new boolean[height][width];
        generate(layers, propertyKey);
    }

    public boolean atScreen( final int x, final int y)
    {
        //FIXME Y axis is inverted on the mask
        int invertedY = (height*Global.TILE_SIZE)-y;
        int gridX = x / Global.TILE_SIZE;
        int gridY = invertedY / Global.TILE_SIZE;

        if(gridX < 0 || gridY < 0 || gridX >= width || gridY >= height){
            //Collide at map borders
            return true;
        }

        return mask[gridY][gridX];
    }

    private void generate(Vector<TileLayer> layers, String propertyKey) {
        //Creates a mask where element [0][0] at top left corner
        for (TileLayer layer : layers) {
            for (int ty = 0; ty < height; ty++) {
                for (int tx = 0; tx < width; tx++) {
                    layer.getTileAt(tx,ty);
                    Tile tile = layer.getTileAt(tx, ty);
                    if ( tile != null && tile.getProperties().containsKey(propertyKey)) {
                        mask[ty][tx] = true;
                    }
                }
            }
        }
    }

    public void printMask(){
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[0].length; j++) {
                if(mask[i][j]){
                    System.out.print("#");
                }else{
                    System.out.print("0");
                }
            }
        }
    }
}
