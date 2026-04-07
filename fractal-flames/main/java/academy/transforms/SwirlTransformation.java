package academy.transforms;

import academy.utils.Point;

public class SwirlTransformation implements Transformation {
    @Override
    public Point transform(Point p) {
        double r = p.x() * p.x() + p.y() * p.y();

        return new Point(p.x() * Math.sin(r) - p.y() * Math.cos(r), p.x() * Math.cos(r) + p.y() * Math.sin(r));
    }
}
