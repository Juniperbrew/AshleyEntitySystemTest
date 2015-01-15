package systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import components.server.Destination;
import components.server.Movement;
import components.shared.Bounds;
import components.shared.Position;
import core.Mappers;
import core.WorldData;

/**
 * Created by Juniperbrew on 14.1.2015.
 */
public class EntityCollisionSystem extends ListeningEntitySystem {

    WorldData worldData;
    String mapName;

    public EntityCollisionSystem(Family family, WorldData worldData, String mapName) {
        super(family);
        this.worldData = worldData;
        this.mapName = mapName;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Position position = Mappers.positionM.get(entity);
        Movement movement = Mappers.movementM.get(entity);
        Position newPosition = new Position(position.x+movement.deltaX,position.y+movement.deltaY);
        Bounds bounds = Mappers.boundsM.get(entity);
        Rectangle playerRectXMovement = new Rectangle(position.x+movement.deltaX, position.y, bounds.width, bounds.height);
        Rectangle playerRectYMovement = new Rectangle(position.x, position.y+movement.deltaY, bounds.width, bounds.height);

        for(Entity e : worldData.getEntitiesInMap(mapName)){
            if(e.equals(entity)){
                //Dont collide with yourself
                continue;
            }
            Position ePosition = Mappers.positionM.get(e);
            Bounds eBounds = Mappers.boundsM.get(e);
            Rectangle eRect = new Rectangle(ePosition.x,ePosition.y,eBounds.width,eBounds.height);
            Rectangle intersection = new Rectangle();
            boolean collided = false;
            if(Intersector.intersectRectangles(eRect, playerRectXMovement, intersection)) {
                movement.deltaX = 0;
                collided = true;
            }
            if(Intersector.intersectRectangles(eRect,playerRectYMovement, intersection)) {
                movement.deltaY = 0;
                collided = true;
            }

            if(collided){
                Destination destination = Mappers.destinationM.get(entity);
                if(destination!=null){
                    AIMoveToDestinationSystem.giveNewDestination(entity);
                }
            }
        }
    }
}