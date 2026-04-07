package academy.transforms;

import academy.utils.Point;

public class ExTransformation implements Transformation {
    @Override
    public Point transform(Point p) {
        double r = Math.sqrt(p.x() * p.x() + p.y() * p.y());
        double theta = Math.atan(p.x() / p.y());

        double p0 = Math.sin(theta + r);
        double p1 = Math.cos(theta - r);

        return new Point(r * (p0 * p0 * p0 + p1 * p1 * p1), r * (p0 * p0 * p0 - p1 * p1 * p1));
    }
}
