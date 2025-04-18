package de.maxhenkel.pipez.gui.sprite;

public class SpriteRect extends SpritePosition {
    public int w;
    public int h;

    public SpriteRect(int x, int y, int w, int h) {
        super(x, y);
        this.w = w;
        this.h = h;
    }
}
