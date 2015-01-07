package components;

import com.badlogic.ashley.core.Component;

public class Health extends Component {

    public int health;
    public int damage;

    public String[] deathSfxId;
    public String[] damageSfxId;
    public String woundParticle;

    public Health(int health) {
        this.health = health;
    }
}