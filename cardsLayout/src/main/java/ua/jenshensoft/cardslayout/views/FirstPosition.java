package ua.jenshensoft.cardslayout.views;


public class FirstPosition {
    private final int hash;
    private int firstPositionX;
    private int firstPositionY;
    private int firstRotation;

    public FirstPosition(int hash) {
        this.hash = hash;
    }

    public FirstPosition(FirstPosition firstPosition) {
        firstPositionX = firstPosition.getFirstPositionX();
        firstPositionY = firstPosition.getFirstPositionY();
        firstRotation = firstPosition.getFirstRotation();
        hash = firstPosition.hash;
    }

    public int getFirstPositionX() {
        return firstPositionX;
    }

    public void setFirstPositionX(int firstPositionX) {
        this.firstPositionX = firstPositionX;
    }

    public int getFirstPositionY() {
        return firstPositionY;
    }

    public void setFirstPositionY(int firstPositionY) {
        this.firstPositionY = firstPositionY;
    }

    public int getFirstRotation() {
        return firstRotation;
    }

    public void setFirstRotation(int firstRotation) {
        this.firstRotation = firstRotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FirstPosition)) return false;
        FirstPosition that = (FirstPosition) o;
        return hash == that.hash;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
