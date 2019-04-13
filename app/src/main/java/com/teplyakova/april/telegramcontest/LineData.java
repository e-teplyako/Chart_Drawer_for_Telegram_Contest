package com.teplyakova.april.telegramcontest;

public class LineData {
    public long[]   posY;
    public String   id;
    public String   name;
    public int      color;

    public LineData (LineData data) {
        posY = data.posY;
        id = data.id;
        name = data.name;
        color = data.color;
    }

    public LineData() {

    }
}
