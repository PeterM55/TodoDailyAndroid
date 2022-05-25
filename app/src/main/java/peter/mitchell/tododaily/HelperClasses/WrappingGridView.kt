package peter.mitchell.tododaily.HelperClasses

import android.content.Context
import android.util.AttributeSet
import android.widget.GridView

class WrappingGridView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : GridView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightSpec = heightMeasureSpec
        if (layoutParams.height == LayoutParams.WRAP_CONTENT)
            heightSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, heightSpec)
    }

}