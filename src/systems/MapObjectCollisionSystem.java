package systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import components.server.Destination;
import components.server.Movement;
import components.shared.Bounds;
import components.shared.Position;
import core.Mappers;
import core.WorldData;
import tiled.core.MapObject;

import java.util.LinkedList;

/**
 * Created by Juniperbrew on 14.1.2015.
 */
public class MapObjectCollisionSystem extends ListeningEntitySystem {

    LinkedList<MapObject> objects;

    public MapObjectCollisionSystem(Family family, WorldData worldData, String mapName) {
        super(family);
        objects = worldData.getObjectsFromMap(mapName);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Position position = Mappers.positionM.get(entity);
        Movement movement = Mappers.movementM.get(entity);
        Bounds bounds = Mappers.boundsM.get(entity);
        Rectangle playerRectXMovement = new Rectangle(position.x+movement.deltaX, position.y, bounds.width, bounds.height);
        Rectangle playerRectYMovement = new Rectangle(position.x, position.y+movement.deltaY, bounds.width, bounds.height);
        boolean collided = false;

        //We only support rectangle objects so this will hopefully crash if we find any other object
        for(MapObject object : objects){
            if(object.getProperties().get("blocked") != null){
                //FIXME this could probably be made more efficient
                java.awt.Rectangle awtRect = object.getBounds();
                Rectangle objRect = new Rectangle(awtRect.x,awtRect.y,awtRect.width,awtRect.height);
                Rectangle intersection = new Rectangle();
                if(Intersector.intersectRectangles(objRect, playerRectXMovement, intersection)) {
                    movement.deltaX = 0;
                    collided = true;
                }
                if(Intersector.intersectRectangles(objRect,playerRectYMovement, intersection)) {
                    movement.deltaY = 0;
                    collided = true;
                }
            }
        }
        if(collided){
            Destination destination = Mappers.destinationM.get(entity);
            if(destination!=null){
                AIMoveToDestinationSystem.giveNewDestination(entity);
            }
        }
    }
}
