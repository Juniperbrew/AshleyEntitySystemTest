package util;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import components.server.Destination;
import components.server.Movement;
import components.shared.*;

public class EntityToString {

    public static String convert(Entity e){
        StringBuilder entityString = new StringBuilder();
        ImmutableArray<Component> components = e.getComponents();

        String positionString = "";
        String healthString = "";
        String nameString = "";
        String mapNameString = "";
        String networkIDString = "";
        String boundsString = "";
        String destinationString = "";
        String movementString = "";
        StringBuilder miscString = new StringBuilder();

        //FIXME exception doesn't crash server but entities stop updating happens with high tickrate and many players
        //com.badlogic.gdx.utils.GdxRuntimeException: #iterator() cannot be used nested.
        //at com.badlogic.gdx.utils.Array$ArrayIterator.hasNext(Array.java:523)
        for(Component component : components){
            if (component instanceof Position) {
                Position pos = (Position) component;
                positionString = ("X:" + pos.x + " Y:" + pos.y + " ");
            } else if (component instanceof Health) {
                Health health = (Health) component;
                healthString = ("Health:" + health.health + " ");
            } else if (component instanceof Name) {
                Name name = (Name) component;
                nameString = ("Name:" + name.name + " ");
            } else if (component instanceof MapName) {
                MapName mapName = (MapName) component;
                mapNameString = ("Map:" + mapName.map + " ");
            } else if (component instanceof NetworkID) {
                NetworkID networkID = (NetworkID) component;
                networkIDString = ("NetworkID:" + networkID.id + " ");
            } else if (component instanceof Bounds) {
                Bounds bounds = (Bounds) component;
                boundsString = ("W:"+bounds.width+" H:"+bounds.height+" ");
            } else if (component instanceof Movement) {
                Movement movement = (Movement) component;
                movementString = ("Dx:"+movement.deltaX+" Dy:"+movement.deltaY+" ");
            } else if (component instanceof Destination) {
                Destination destination = (Destination) component;
                destinationString = ("DestX:"+destination.x+" DestY:"+destination.y+" ");
            }else {
                miscString.append(component.getClass().getSimpleName() + " ");
            }
        }
        entityString
                .append(e.getId() + ": ")
                .append(networkIDString)
                .append(nameString)
                .append(mapNameString)
                .append(boundsString)
                .append(positionString)
                .append(movementString)
                .append(healthString)
                .append(destinationString)
                .append(miscString);
        return entityString.toString();
    }
}