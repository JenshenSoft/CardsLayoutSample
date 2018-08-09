package ua.jenshensoft.cardslayoutsample.views;


public class CardInfoModel {

    private final int position;
    private final int number;

    public CardInfoModel(int position, int number) {
        this.position = position;
        this.number = number;
    }

    public int getPosition() {
        return position;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CardInfoModel that = (CardInfoModel) o;

        return number == that.number;
    }

    @Override
    public int hashCode() {
        return number;
    }
}