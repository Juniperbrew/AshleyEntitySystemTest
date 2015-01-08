package util;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import components.*;

public class EntityToString {

    public static String convert(Entity e){
        StringBuilder entityString = new StringBuilder();
        ImmutableArray<Component> components = e.getComponents();

        String positionString = "";
        String healthString = "";
        String nameString = "";
        String mapNameString = "";
        String networkIDString = "";
        StringBuilder miscString = new StringBuilder();

        for(Component component : components){
            if (component instanceof Position) {
                Position pos = (Position) component;
                positionString = ("X: " + pos.x + " Y: " + pos.y + " ");
            } else if (component instanceof Health) {
                Health health = (Health) component;
                healthString = ("Health: " + health.health + " ");
            } else if (component instanceof Name) {
                Name name = (Name) component;
                nameString = ("Name: " + name.name + " ");
            } else if (component instanceof MapName) {
                MapName mapName = (MapName) component;
                mapNameString = ("Map: " + mapName.map + " ");
            } else if (component instanceof NetworkID) {
                NetworkID networkID = (NetworkID) component;
                networkIDString = ("NetworkID: " + networkID.id + " ");
            }else {
                miscString.append(component.getClass() + " ");
            }
        }
        entityString
                .append(e.getId() + ": ")
                .append(networkIDString)
                .append(nameString)
                .append(mapNameString)
                .append(positionString)
                .append(healthString)
                .append(miscString);
        return entityString.toString();
    }
}