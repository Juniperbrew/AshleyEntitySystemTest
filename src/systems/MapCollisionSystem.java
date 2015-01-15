package systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import components.server.Destination;
import components.server.Movement;
import components.shared.Bounds;
import components.shared.Name;
import components.shared.Position;
import core.Global;
import core.Mappers;
import core.WorldData;
import util.MapMask;

import java.util.Vector;

/**
 * Created by Juniperbrew on 14.1.2015.
 */
public class MapCollisionSystem extends ListeningEntitySystem {

    private Family family;
    MapMask solidMask;
    WorldData worldData;
    String mapName;
    Engine engine;

    public MapCollisionSystem(Family family, WorldData worldData, String mapName) {
        super(family);
        this.worldData = worldData;
        this.mapName = mapName;
        solidMask = worldData.getMapMask(mapName,"blocked");
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Movement delta = Mappers.movementM.get(entity);
        if(delta.deltaY == 0 && delta.deltaX == 0){
            //No movement no collision
            return;
        }
        Position pos = Mappers.positionM.get(entity);
        Bounds bounds = Mappers.boundsM.get(entity);
        Position newPos = new Position(pos.x+delta.deltaX,pos.y+delta.deltaY);
        boolean collided = false;

        //FIXME Is it enough to use the midpoints of each side as collision point?
        //Find mid point on left and right side of collision box and check if they are inside a tile
        if((delta.deltaX > 0 && collides((int)newPos.x+bounds.width,(int)newPos.y+(bounds.height/2)))
                || (delta.deltaX < 0 && collides((int) newPos.x, (int) newPos.y + (bounds.height/2)))){
            delta.deltaX = 0;
            //FIXME reverting move should cause jittering when colliding?
            newPos.x = pos.x;
            collided = true;
        }
        //Find mid point on top and bottom side of collision box and check if they are inside a tile
        if((delta.deltaY > 0 && collides((int)newPos.x+(bounds.width/2),(int)newPos.y+bounds.height))
                || (delta.deltaY < 0 && collides((int)newPos.x +(bounds.width/2),(int)newPos.y))){
            delta.deltaY = 0;
            collided = true;
        }
        if(collided){
            Destination destination = Mappers.destinationM.get(entity);
            if(destination!=null){
                AIMoveToDestinationSystem.giveNewDestination(entity);
            }
        }
    }

    private boolean collides(int x, int y){
        return solidMask.atScreen(x,y);
    }
}
