package core;

import com.badlogic.ashley.core.ComponentMapper;
import components.server.Destination;
import components.server.Movement;
import components.server.Target;
import components.shared.*;

import static com.badlogic.ashley.core.ComponentMapper.*;

public class Mappers {
    public static final ComponentMapper<Position> positionM = getFor(Position.class);
    public static final ComponentMapper<Velocity> velocityM = getFor(Velocity.class);
    public static final ComponentMapper<Name> nameM = getFor(Name.class);
    public static final ComponentMapper<MapName> mapM = getFor(MapName.class);
    public static final ComponentMapper<NetworkID> networkidM = getFor(NetworkID.class);
    public static final ComponentMapper<Player> playerM = getFor(Player.class);
    public static final ComponentMapper<TileID> tileidM = getFor(TileID.class);
    public static final ComponentMapper<Target> targetM = getFor(Target.class);
    public static final ComponentMapper<Destination> destinationM = getFor(Destination.class);
    public static final ComponentMapper<Bounds> boundsM = getFor(Bounds.class);
    public static final ComponentMapper<Movement> movementM = getFor(Movement.class);

}