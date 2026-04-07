package academy.transforms;

import academy.utils.Point;

public class SphericalTransformation implements Transformation {
    @Override
    public Point transform(Point p) {
        double k = 1 / (p.x() * p.x() + p.y() * p.y());
        return new Point(k * p.x(), k * p.y());
    }
}
