package academy.transforms;

import academy.utils.Point;

public class HandkerchiefTransformation implements Transformation {
    @Override
    public Point transform(Point p) {
        double r = Math.sqrt(p.x() * p.x() + p.y() * p.y());
        double theta = Math.atan(p.x() / p.y());

        return new Point(r * Math.sin(theta + r), r * Math.cos(theta - r));
    }
}
