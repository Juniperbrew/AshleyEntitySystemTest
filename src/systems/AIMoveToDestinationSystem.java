package systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import components.server.Destination;
import components.server.Movement;
import components.server.Target;
import components.shared.Position;
import core.Mappers;

import java.util.Vector;

/**
 * Created by Juniperbrew on 12.1.2015.
 */
public class AIMoveToDestinationSystem extends ListeningEntitySystem {

    private float speed = 75; //pixels per sec

    public AIMoveToDestinationSystem(Family family) {
        super(family);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Destination destination = Mappers.destinationM.get(entity);

        int destX = destination.x;
        int destY = destination.y;

        Position ownPosition = Mappers.positionM.get(entity);

        //FIXME is this the most efficient way
        float deltaX = destX - ownPosition.x;
        float deltaY = destY - ownPosition.y;

        if(deltaX == 0 && deltaY == 0){
            //Destination has been reached give the entity a new destination
            giveNewDestination(entity);
            //Start moving at next iteration
            return;
        }

        float distanceMoved = speed*deltaTime;
        float fullDistance = (float) Math.sqrt(Math.pow(deltaX,2) + Math.pow(deltaY,2));

        Movement movement = Mappers.movementM.get(entity);
        movement.deltaX = (deltaX*distanceMoved)/fullDistance;
        movement.deltaY = (deltaY*distanceMoved)/fullDistance;

        if(movement.deltaX > 0 && movement.deltaX > deltaX){
            movement.deltaX = deltaX;
        }
        if(movement.deltaX < 0 && movement.deltaX < deltaX){
            movement.deltaX = deltaX;
        }
        if(movement.deltaY > 0 && movement.deltaY > deltaY){
            movement.deltaY = deltaY;
        }
        if(movement.deltaY < 0 && movement.deltaY < deltaY){
            movement.deltaY = deltaY;
        }
    }

    public static void giveNewDestination(Entity e){
        Destination dest = Mappers.destinationM.get(e);
        Position pos = Mappers.positionM.get(e);
        int direction = MathUtils.random(360);
        int distance = MathUtils.random(100,200);
        dest.x = (int) (pos.x+MathUtils.cosDeg(direction)*distance);
        dest.y = (int) (pos.y+MathUtils.sinDeg(direction)*distance);
    }
}
