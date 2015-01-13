package components.server;

import com.badlogic.ashley.core.Component;

/**
 * Created by Juniperbrew on 12.1.2015.
 */
public class Destination extends Component{

    public int x;
    public int y;

    public Destination(int x, int y) {

        this.x = x;
        this.y = y;
    }
}
