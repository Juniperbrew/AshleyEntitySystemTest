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
public class AIFollowEntitySystem extends EntitySystem implements EntityListener{

    private Family family;
    private float speed = 5; //pixels per sec
    Vector<Entity> entities;

    public AIFollowEntitySystem(Family family) {
        this.family = family;
        entities = new Vector<>();
    }

    @Override
    public void update (float deltaTime) {
        for (Entity entity : entities) {
            Entity target = Mappers.targetM.get(entity).target;

            if(target == null){
                entity.remove(Target.class);
                continue;
            }

            //System.out.println("Moving "+Mappers.nameM.get(entity).name+" to target.");

            Position targetPosition = Mappers.positionM.get(target);
            Position ownPosition = Mappers.positionM.get(entity);
            Movement movement = Mappers.movementM.get(entity);

            //System.out.println(Mappers.nameM.get(entity).name+" is trying to follow "+Mappers.nameM.get(target).name);

            //FIXME is this the most efficient way
            float deltaX = targetPosition.x - ownPosition.x;
            float deltaY = targetPosition.y - ownPosition.y;

            if(deltaX == 0 && deltaY == 0){
                //Dont do anything if we are at target, without this we get divided by 0 errors in current implementation
                continue;
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


    @Override
    public void addedToEngine (Engine engine) {
        //FIXME can i somehow use ImmutableArray instead of Vector
        ImmutableArray<Entity> immutableEntities = engine.getEntitiesFor(family);
        for(Entity e : immutableEntities){
            System.out.println("Entity (" + Mappers.nameM.get(e).name + ") added to AIFollow");
            entities.add(e);
        }
    }

    @Override
    public void removedFromEngine (Engine engine) {
        entities.clear();
    }

    @Override
    public void entityAdded(Entity entity) {
        System.out.println(Mappers.nameM.get(entity).name + " has been given a target.");
        entities.add(entity);
    }

    @Override
    public void entityRemoved(Entity entity) {
        System.out.println(Mappers.nameM.get(entity).name + " has lost its target.");
        entities.remove(entity);
    }
}
