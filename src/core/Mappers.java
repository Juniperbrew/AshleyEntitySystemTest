package core;

import com.badlogic.ashley.core.ComponentMapper;
import components.Position;
import components.Velocity;

public class Mappers {
    public static final ComponentMapper<Position> positionM = ComponentMapper.getFor(Position.class);
    public static final ComponentMapper<Velocity> velocityM = ComponentMapper.getFor(Velocity.class);
}