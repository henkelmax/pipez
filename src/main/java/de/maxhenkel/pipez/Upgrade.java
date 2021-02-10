package de.maxhenkel.pipez;

public enum Upgrade {

    BASIC("basic"), IMPROVED("improved"), ADVANCED("advanced"), ULTIMATE("ultimate"), INFINITY("infinity");

    private final String name;

    Upgrade(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
