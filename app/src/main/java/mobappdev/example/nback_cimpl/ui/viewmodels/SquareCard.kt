package mobappdev.example.nback_cimpl.ui.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.CornerRounding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SquareCard {


    private var _secondColor = MutableStateFlow(Color(0));
    public val secondColor: StateFlow<Color>
        get() = _secondColor

    private var _color = MutableStateFlow<Color>(Color(0));
    public val color: StateFlow<Color>
        get() = _color

    constructor(color: Color){
        this._color.value = color;
        this._secondColor.value = calculateNewColor();
    }

    public fun setColor(color:Color){
        _color.value = color;
        _secondColor.value = calculateNewColor()
    }

    private fun calculateNewColor():Color{
        return Color(alpha=0.5f,
            red= if (color.value.red+0.1f <= 1) color.value.red+0.1f else 1f,
            blue= if (color.value.blue+0.1f <= 1) color.value.blue+0.1f else 1f,
            green= if (color.value.green+0.1f <= 1) color.value.green+0.1f else 1f
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