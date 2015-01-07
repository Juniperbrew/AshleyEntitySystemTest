package components;

import com.badlogic.ashley.core.Component;

public class Name extends Component {
    public String name;

    public Name(String name) {
        this.name = name;
    }
}