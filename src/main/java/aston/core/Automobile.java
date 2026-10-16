package aston.core;

public class Automobile {
    private final String model;
    private final int power;
    private final int year;

    private Automobile(Builder builder) {
        this.model = builder.model;
        this.power = builder.power;
        this.year = builder.year;
    }

    public String getModel() { return model; }
    public int getPower() { return power; }
    public int getYear() { return year; }

    @Override
    public String toString() {
        return "Automobile{" + "model='" + model + '\'' + ", power=" + power + ", year=" + year + '}';
    }

    public static class Builder {
        private String model;
        private int power;
        private int year;

        public Builder model(String model) { this.model = model; return this; }
        public Builder power(int power) { this.power = power; return this; }
        public Builder year(int year) { this.year = year; return this; }
        public Automobile build() { return new Automobile(this); }
    }
}