package components.shared;

import com.badlogic.ashley.core.Component;

public class Position extends Component {
    public float x;
    public float y;

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Position() {
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
}