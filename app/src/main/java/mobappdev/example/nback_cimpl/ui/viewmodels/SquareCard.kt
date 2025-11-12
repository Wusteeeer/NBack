package mobappdev.example.nback_cimpl.ui.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.CornerRounding

class SquareCard {


    private var _secondColor: Color;
    public val secondColor: Color
        get() = _secondColor

    private var _color: Color;
    public var color: Color
        get() = _color
        set(value:Color){
            _color = value;
            _secondColor = calculateNewColor()
        }

    constructor(color: Color){
        this._color = color;
        this._secondColor = calculateNewColor();
    }

    private fun calculateNewColor():Color{
        return Color(alpha=0.5f,
            red= if (color.red+0.1f <= 1) color.red+0.1f else 1f,
            blue= if (color.blue+0.1f <= 1) color.blue+0.1f else 1f,
            green= if (color.green+0.1f <= 1) color.green+0.1f else 1f
        )
    }

    public fun createSquare(minDimension: Float, width: Float, height: Float): RoundedPolygon{
        return RoundedPolygon(
            numVertices = 4,
            radius=minDimension/2f,
            centerX=width/2f,
            centerY=height/2f
        );
    }

    public fun createRoundedSquare(minDimension: Float, width: Float, height: Float, radius: Float, smoothing: Float): RoundedPolygon{
        return RoundedPolygon(
            numVertices = 4,
            radius=minDimension/2f,
            centerX=width/2f,
            centerY=height/2f,
            rounding = CornerRounding(
                radius=radius,
                smoothing=smoothing
            )
        );
    }

}