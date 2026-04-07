package academy.model;

public record Image(Pixel[] data, int width, int height) {
    public static Image create(int width, int height) {
        Pixel[] data = new Pixel[width * height];

        for (int i = 0; i < data.length; i++) {
            data[i] = new Pixel(0, 0, 0, 0);
        }

        return new Image(data, width, height);
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public Pixel pixel(int x, int y) {
        return data[x + y * width];
    }
}
