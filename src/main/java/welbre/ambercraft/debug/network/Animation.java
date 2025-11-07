package welbre.ambercraft.debug.network;

import java.util.function.Function;

public class Animation extends Scheduler.EachTickTask {
    private final NetworkWidget widget;
    private final int x_delta;
    private final int y_delta;
    private final int width_delta;
    private final int height_delta;
    private final int backGround_delta;
    private final int x_ini;
    private final int y_ini;
    private final int width_ini;
    private final int height_ini;
    private final int backGround_ini;
    private final Function<Double, Double> interpolation;

    /**
     * @param duration in seconds!
     */
    public Animation(NetworkWidget widget, int x, int y, int width, int height, int backGround, float duration, Function<Double, Double> interpolation) {
        super(duration, (task) -> {}, Animation::animate);
        this.widget = widget;
        this.interpolation = interpolation;

        this.x_ini = widget.getX();
        this.y_ini = widget.getY();
        this.width_ini = widget.getWidth();
        this.height_ini = widget.getHeight();
        this.backGround_ini = widget.color;

        this.x_delta = x - x_ini;
        this.y_delta = y - y_ini;
        this.width_delta = width - width_ini;
        this.height_delta = height - height_ini;
        this.backGround_delta = backGround - backGround_ini;
    }

    /**
     * @param duration in seconds!
     */
    public Animation(NetworkWidget widget, int x, int y, float duration) {
        this(widget, x, y, widget.getWidth(), widget.getHeight(), widget.color, duration, Interpolations.LINEAR);
    }

    /**
     * @param duration in seconds!
     */
    public Animation(NetworkWidget widget, int x, int y, float duration, Function<Double, Double> inter) {
        this(widget, x, y, widget.getWidth(), widget.getHeight(), widget.color, duration, inter);
    }

    public void animate() {
        double progress = (double) time / delay;
        progress = Math.min(interpolation.apply(progress), 1.0);

        widget.setX((int) (x_ini + x_delta * progress));
        widget.setY((int) (y_ini + y_delta * progress));
        widget.setWidth((int) (width_ini + width_delta * progress));
        widget.setHeight((int) (height_ini + height_delta * progress));
        widget.color = (int) (backGround_ini + backGround_delta * progress);
    }

    public static void animate(Scheduler.Task task) {
        Animation animation = (Animation) task;
        animation.animate();
    }

    @SuppressWarnings("unused")
    public enum Interpolations implements Function<Double, Double> {
        LINEAR {
            @Override
            public Double apply(Double t) {
                return t;
            }
        },
        EASE_OUT_QUART {
            @Override
            public Double apply(Double t) {
                return 1.0 - Math.pow(1.0 - t, 4.0);
            }
        }
    }
}
