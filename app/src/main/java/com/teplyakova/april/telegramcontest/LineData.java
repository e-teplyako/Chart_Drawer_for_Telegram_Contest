package com.teplyakova.april.telegramcontest;

public class LineData {
    private int[] _points;
    private String _id;
    private String _name;
    private int _color;

    public LineData(LineData data) {
        _points = data.getPoints();
        _id = data.getId();
        _name = data.getName();
        _color = data.getColor();
    }

    public LineData() {

    }

    public int[] getPoints() {
        return _points.clone();
    }

    public void setPoints(int[] points) {
        _points = points;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public int getColor() {
        return _color;
    }

    public void setColor(int color) {
        _color = color;
    }
}
