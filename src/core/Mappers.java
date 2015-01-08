package core;

import com.badlogic.ashley.core.ComponentMapper;
import components.*;

public class Mappers {
    public static final ComponentMapper<Position> positionM = ComponentMapper.getFor(Position.class);
    public static final ComponentMapper<Velocity> velocityM = ComponentMapper.getFor(Velocity.class);
    public static final ComponentMapper<Name> nameM = ComponentMapper.getFor(Name.class);
    public static final ComponentMapper<MapName> mapM = ComponentMapper.getFor(MapName.class);
    public static final ComponentMapper<NetworkID> idM = ComponentMapper.getFor(NetworkID.class);
}