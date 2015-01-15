package systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import components.server.Movement;
import components.server.Target;
import components.shared.NetworkID;
import components.shared.Position;
import core.Mappers;

import java.util.Vector;

/**
 * Created by Juniperbrew on 12.1.2015.
 */
public class AIFollowEntitySystem extends ListeningEntitySystem{

    private float speed = 75; //pixels per sec

    public AIFollowEntitySystem(Family family) {
        super(family);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Entity target = Mappers.targetM.get(entity).target;

        if(target == null){
            entity.remove(Target.class);
            return;
        }

        Position targetPosition = Mappers.positionM.get(target);
        Position ownPosition = Mappers.positionM.get(entity);
        Movement movement = Mappers.movementM.get(entity);

        //FIXME is this the most efficient way
        float deltaX = targetPosition.x - ownPosition.x;
        float deltaY = targetPosition.y - ownPosition.y;

        if(deltaX == 0 && deltaY == 0){
            //Dont do anything if we are at target, without this we get divided by 0 errors in current implementation
            return;
        }

        float distanceMoved = speed*deltaTime;
        float fullDistance = (float) Math.sqrt(Math.pow(deltaX,2) + Math.pow(deltaY,2));

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
}
