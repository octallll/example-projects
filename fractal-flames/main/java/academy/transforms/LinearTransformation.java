package academy.transforms;

import academy.utils.Point;

public class LinearTransformation implements Transformation {
    @Override
    public Point transform(Point p) {
        return p;
    }
}
