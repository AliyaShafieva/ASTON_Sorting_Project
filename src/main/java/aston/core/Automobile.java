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

    /**
     * Сравнивает текущий автомобиль с другим объектом.
     * Автомобили считаются равными, если у них совпадают все ключевые поля:
     * модель, мощность и год выпуска.
     * @param o объект для сравнения
     * @return true, если объекты равны по значению полей, иначе false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Проверка на ссылку в памяти (быстрая проверка)
        if (o == null || getClass() != o.getClass()) return false; // Проверка на null и тип класса
        Automobile that = (Automobile) o;
        // Сравнение примитивов и объектов через Objects.equals для безопасности
        return power == that.power && year == that.year && java.util.Objects.equals(model, that.model);
    }

    /**
     * Вычисляет хэш-код на основе полей model, power и year.
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        // Генерируем хэш на основе тех же полей, что участвуют в equals
        return java.util.Objects.hash(model, power, year);
    }
}