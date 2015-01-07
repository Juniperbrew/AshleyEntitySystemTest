package util;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import components.Health;
import components.Position;

public class EntityToString {

    public static String convert(Entity e){
        StringBuilder entityString = new StringBuilder();
        ImmutableArray<Component> components = e.getComponents();
        entityString.append(e + " ");

        for(Component component : components){
            if (component instanceof Position) {
                Position pos = (Position) component;
                entityString.append("X: " + pos.x + " Y: " + pos.y + " ");
            } else if (component instanceof Health) {
                Health health = (Health) component;
                entityString.append("Health: " + health.health + " ");
            } else {
                entityString.append(component.getClass() + " ");
            }
        }
        return entityString.toString();
    }
}