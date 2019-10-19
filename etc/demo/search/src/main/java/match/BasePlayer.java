package match;

import domains.MNK;

/**
 * A base class for MNK players to extend.
 */
public abstract class BasePlayer implements MNKPlayer {

    protected int side;

    public BasePlayer(int side) {
        assert side == MNK.X || side == MNK.O;
        this.side = side;
    }

    @Override
    public int getSide() {
        return side;
    }

}
