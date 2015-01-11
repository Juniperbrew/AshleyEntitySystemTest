package core;

import com.badlogic.ashley.core.ComponentMapper;
import components.*;

public class Mappers {
    public static final ComponentMapper<Position> positionM = ComponentMapper.getFor(Position.class);
    public static final ComponentMapper<Velocity> velocityM = ComponentMapper.getFor(Velocity.class);
    public static final ComponentMapper<Name> nameM = ComponentMapper.getFor(Name.class);
    public static final ComponentMapper<MapName> mapM = ComponentMapper.getFor(MapName.class);
    public static final ComponentMapper<NetworkID> networkidM = ComponentMapper.getFor(NetworkID.class);
    public static final ComponentMapper<Player> playerM = ComponentMapper.getFor(Player.class);
    public static final ComponentMapper<TileID> tileidM = ComponentMapper.getFor(TileID.class);
}