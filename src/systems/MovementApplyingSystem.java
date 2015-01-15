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
public class MovementApplyingSystem extends ListeningEntitySystem {

    public MovementApplyingSystem(Family family) {
        super(family);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Movement movement = Mappers.movementM.get(entity);
        Position position = Mappers.positionM.get(entity);

        position.x += movement.deltaX;
        position.y += movement.deltaY;

        movement.deltaX = 0;
        movement.deltaY = 0;
    }
}