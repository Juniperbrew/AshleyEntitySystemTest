package components.shared;

import com.badlogic.ashley.core.Component;

/**
 * Created by Juniperbrew on 13.1.2015.
 */
public class Bounds extends Component {
    public int width;
    public int height;

    public Bounds() {
    }

    public Bounds(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
