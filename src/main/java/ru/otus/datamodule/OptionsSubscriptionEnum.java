package ru.otus.datamodule;

public enum OptionsSubscriptionEnum {
    BASIC_M6(0, "Basic", 80000),
    BASIC_M12(0, "Basic", 120000),
    STANDARD_M6(1, "Standard", 193000),
    STANDARD_M12(1, "Standard", 289000),
    PROFESSIONAL_M6(2, "Professional", 210000),
    PROFESSIONAL_M12(2, "Professional", 315000);

    private final int id;
    private final String name;
    private final int amount;

    OptionsSubscriptionEnum(final int id, final String name, final int amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public String getOptionName() {
        return name;
    }

    public int getOptionAmount() {
        return amount;
    }
}
