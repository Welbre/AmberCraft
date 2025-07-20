package welbre.ambercraft.debug

import java.util.function.Function
import kotlin.math.pow

class Animation(
    private val node: ScreenNode,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    backGround: Int,
    private val duration: Int,
    private val interpolation: Function<Double, Double>
) {
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
    private var time = 0f
    private var isDone = false

    constructor(node: ScreenNode, x: Int, y: Int, duration: Int) : this(
        node,
        x,
        y,
        node.width,
        node.height,
        node.backGround,
        duration,
        Interpolations.EASE_OUT_QUART
    )

    constructor(node: ScreenNode, x: Int, y: Int, duration: Int, inter: Function<Double, Double>) : this(
        node,
        x,
        y,
        node.width,
        node.height,
        node.backGround,
        duration,
        inter
    )

    init {
        this.x_ini = node.x
        this.y_ini = node.y
        this.width_ini = node.width
        this.height_ini = node.height
        this.backGround_ini = node.backGround
        this.x_delta = x - x_ini
        this.y_delta = y - y_ini
        this.width_delta = width - width_ini
        this.height_delta = height - height_ini
        this.backGround_delta = backGround - backGround_ini
    }


    @JvmOverloads
    fun tick(amount: Float = 1f) {
        time += amount
        if (time >= duration) {
            isDone = true
            return
        }

        var progress = time.toDouble() / duration
        progress = interpolation.apply(progress)
        node.x = (x_ini + x_delta * progress).toInt()
        node.y = (y_ini + y_delta * progress).toInt()
        node.width = (width_ini + width_delta * progress).toInt()
        node.height = (height_ini + height_delta * progress).toInt()
        node.backGround = (backGround_ini + backGround_delta * progress).toInt()
    }

    fun done(): Boolean {
        return isDone
    }

    enum class Interpolations : Function<Double, Double> {
        LINEAR {
            override fun apply(t: Double): Double = t
        },
        EASE_OUT_QUART {
            override fun apply(t: Double): Double = 1.0 - (1.0 - t).pow(4.0)
        }
    }
}
