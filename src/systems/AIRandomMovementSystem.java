package systems;


import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import components.server.Movement;
import components.shared.Position;
import core.Mappers;

import java.util.Random;

public class AIRandomMovementSystem extends IteratingSystem {

    Random rng;

    public AIRandomMovementSystem(Family family) {
        super(family);
        rng = new Random();
    }

    public  AIRandomMovementSystem (Family family, int priority) {
        super(family, priority);
        rng = new Random();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Position position = Mappers.positionM.get(entity);
        Movement movement = Mappers.movementM.get(entity);

        if(Mappers.targetM.get(entity) != null){
            System.out.println("Randomly moving " +Mappers.nameM.get(entity).name);
        }
        movement.deltaX = ((rng.nextFloat() * 5) - 2.5f) * deltaTime;
        movement.deltaY = ((rng.nextFloat() * 5) - 2.5f) * deltaTime;
    }
}
