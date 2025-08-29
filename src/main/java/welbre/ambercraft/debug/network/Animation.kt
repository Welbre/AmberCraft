package welbre.ambercraft.debug.network

import java.util.function.Function
import kotlin.math.pow

class Animation(
    private val widget: NetworkWidget,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    backGround: Int,
    duration: Float,
    private val interpolation: Function<Double, Double>,
) : Scheduler.EachTickTask(duration, {},this::animate)
{
    private val x_delta: Int
    private val y_delta: Int
    private val width_delta: Int
    private val height_delta: Int
    private val backGround_delta: Int
    private val x_ini: Int
    private val y_ini: Int
    private val width_ini: Int
    private val height_ini: Int
    private val backGround_ini: Int

    /**
     * @param duration in seconds!
     */
    constructor(widget: NetworkWidget, x: Int, y: Int, duration: Float) : this(
        widget,
        x,
        y,
        widget.width,
        widget.height,
        widget.color,
        duration,
        Interpolations.LINEAR,
    )

    /**
     * @param duration in seconds!
     */
    constructor(widget: NetworkWidget, x: Int, y: Int, duration: Float, inter: Function<Double, Double>) : this(
        widget,
        x,
        y,
        widget.width,
        widget.height,
        widget.color,
        duration,
        inter,
    )

    init {
        this.x_ini = widget.x
        this.y_ini = widget.y
        this.width_ini = widget.width
        this.height_ini = widget.height
        this.backGround_ini = widget.color
        this.x_delta = x - x_ini
        this.y_delta = y - y_ini
        this.width_delta = width - width_ini
        this.height_delta = height - height_ini
        this.backGround_delta = backGround - backGround_ini
    }

    fun animate() {
        var progress = time.toDouble() / delay
        progress = Math.min(interpolation.apply(progress), 1.0)
        widget.x = (x_ini + x_delta * progress).toInt()
        widget.y = (y_ini + y_delta * progress).toInt()
        widget.width = (width_ini + width_delta * progress).toInt()
        widget.height = (height_ini + height_delta * progress).toInt()
        widget.color = (backGround_ini + backGround_delta * progress).toInt()
    }

    companion object
    {
        fun animate(task: Scheduler.Task)
        {
            val animation = task as Animation
            animation.animate()
        }
    }

    @SuppressWarnings("unused")
    enum class Interpolations : Function<Double, Double> {
        LINEAR {
            override fun apply(t: Double): Double = t
        },
        EASE_OUT_QUART {
            override fun apply(t: Double): Double = 1.0 - (1.0 - t).pow(4.0)
        }
    }
}