package util;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import components.Health;
import components.MapName;
import components.Name;
import components.Position;

public class EntityToString {

    public static String convert(Entity e){
        StringBuilder entityString = new StringBuilder();
        ImmutableArray<Component> components = e.getComponents();
        entityString.append(e.getId() + ": ");

        for(Component component : components){
            if (component instanceof Position) {
                Position pos = (Position) component;
                entityString.append("X: " + pos.x + " Y: " + pos.y + " ");
            } else if (component instanceof Health) {
                Health health = (Health) component;
                entityString.append("Health: " + health.health + " ");
            } else if (component instanceof Name) {
                Name name = (Name) component;
                entityString.append("Name: " + name.name + " ");
            } else if (component instanceof MapName) {
                MapName mapName = (MapName) component;
                entityString.append("Map: " + mapName.map + " ");
            }else {
                entityString.append(component.getClass() + " ");
            }
        }
        return entityString.toString();
    }
}