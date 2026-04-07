package academy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Pixel {
    private int r;
    private int g;
    private int b;
    private int hitCount;

    public void addHit(int colorR, int colorG, int colorB) {
        if (hitCount == 0) {
            this.r = colorR;
            this.g = colorG;
            this.b = colorB;
        } else {
            double newHitCount = hitCount + 1.0;

            this.r = (int) Math.round((this.r * hitCount + colorR) / newHitCount);
            this.g = (int) Math.round((this.g * hitCount + colorG) / newHitCount);
            this.b = (int) Math.round((this.b * hitCount + colorB) / newHitCount);
        }

        hitCount++;
    }
}
