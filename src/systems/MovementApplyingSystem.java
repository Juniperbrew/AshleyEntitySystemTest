package systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import components.server.Movement;
import components.shared.Position;
import core.Mappers;

/**
 * Created by Juniperbrew on 14.1.2015.
 */
public class MovementApplyingSystem extends IteratingSystem {

    Engine engine;
    public MovementApplyingSystem(Family family) {
        super(family);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Movement movement = Mappers.movementM.get(entity);
        Position position = Mappers.positionM.get(entity);

        position.x += movement.deltaX;
        position.y += movement.deltaY;

        /*if (Mappers.targetM.get(entity) != null) {
            System.out.println(Mappers.nameM.get(entity).name + "> Dx:" + movement.deltaX + " Dy:" + movement.deltaY + " DeltaTime:" + deltaTime);
        }*/

        movement.deltaX = 0;
        movement.deltaY = 0;
    }

    public void addedToEngine (Engine engine) {
        super.addedToEngine(engine);
        this.engine = engine;
        System.out.println(this.toString()+" added to "+engine.toString());
    }
}