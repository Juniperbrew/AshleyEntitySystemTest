package systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import components.server.Destination;
import components.server.Target;
import components.shared.Position;
import core.Mappers;

import java.util.Vector;

/**
 * Created by Juniperbrew on 12.1.2015.
 */
public class AIMoveToDestinationSystem extends EntitySystem implements EntityListener {

    private Family family;
    private float speed = 5; //pixels per sec
    Vector<Entity> entities;
    public AIMoveToDestinationSystem(Family family) {
        this.family = family;
        entities = new Vector<>();
    }

    @Override
    public void update (float deltaTime) {
        for (Entity entity : entities) {
            Destination destination = Mappers.destinationM.get(entity);

            int destX = destination.x;
            int destY = destination.y;

            Position ownPosition = Mappers.positionM.get(entity);

            //System.out.println(Mappers.nameM.get(entity).name+" is trying to follow "+Mappers.nameM.get(target).name);

            //FIXME is this the most efficient way
            float deltaX = destX - ownPosition.x;
            float deltaY = destY - ownPosition.y;

            if(deltaX == 0 && deltaY == 0){
                //Destination has been reached give the entity a new destination
                int direction = MathUtils.random(360);
                int distance = MathUtils.random(100,200);
                destination.x += (int) (MathUtils.cosDeg(direction)*distance);
                destination.y += (int) (MathUtils.sinDeg(direction)*distance);
                System.out.println("New destination of "+Mappers.nameM.get(entity).name+" is X:"+destination.x+" Y:"+destination.y);
                //Start moving at next iteration
                continue;
            }

            float distanceMoved = speed*deltaTime;
            float fullDistance = (float) Math.sqrt(Math.pow(deltaX,2) + Math.pow(deltaY,2));

            float movementX = (deltaX*distanceMoved)/fullDistance;
            float movementY = (deltaY*distanceMoved)/fullDistance;

            //FIXME this clamping bugs when target is reached
            if(movementX > 0 && movementX > deltaX){
                movementX = deltaX;
                System.out.println(movementX);
            }
            if(movementX < 0 && movementX < deltaX){
                movementX = deltaX;
                System.out.println(movementX);
            }
            if(movementY > 0 && movementY > deltaY){
                movementY = deltaY;
                System.out.println(movementY);
            }
            if(movementY < 0 && movementY < deltaY){
                movementY = deltaY;
                System.out.println(movementY);
            }

            ownPosition.x += movementX;
            ownPosition.y += movementY;
        }
    }


    @Override
    public void addedToEngine (Engine engine) {
        //FIXME can i somehow use ImmutableArray instead of Vector
        ImmutableArray<Entity> immutableEntities = engine.getEntitiesFor(family);
        for(Entity e : immutableEntities){
            System.out.println("Entity (" + Mappers.nameM.get(e).name + ") added to AIMoveToDestination");
            entities.add(e);
        }
    }

    @Override
    public void removedFromEngine (Engine engine) {
        entities.clear();
    }

    @Override
    public void entityAdded(Entity entity) {
        System.out.println(Mappers.nameM.get(entity).name + " has been given a destination.");
        entities.add(entity);
    }

    @Override
    public void entityRemoved(Entity entity) {
        System.out.println(Mappers.nameM.get(entity).name + " has lost its destination.");
        entities.remove(entity);
    }
}
