package peter.mitchell.tododaily.HelperClasses

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import kotlinx.android.synthetic.main.fragment_home.*
import org.w3c.dom.Text
import peter.mitchell.tododaily.saveInformation

class TextGridLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : GridLayout(context, attrs) {

    public var textGrid : ArrayList<TextView> = ArrayList<TextView>(25)

    private val layoutParams : ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
        resources.displayMetrics.widthPixels/2, // width
        ConstraintLayout.LayoutParams.WRAP_CONTENT,
    )

    init {

        // setup layoutParams
        layoutParams.topMargin = 5
        layoutParams.leftMargin = 3
        layoutParams.bottomMargin = 20

    }

    public fun addString(context : Context, str : String) {
        textGrid.add(TextView(context))
        textGrid[textGrid.size-1].setText(str)
        textGrid[textGrid.size-1].textSize = 18f
        textGrid[textGrid.size-1].setTextColor(Color.BLACK)
        textGrid[textGrid.size-1].textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
        textGrid[textGrid.size-1].minHeight = 30
        textGrid[textGrid.size-1].id = View.generateViewId()
        textGrid[textGrid.size-1].layoutParams = layoutParams
        this.addView(textGrid[textGrid.size-1])
    }

    public fun setAdapter(context : Context, strings : ArrayList<String>) {
        for (i in 0 until textGrid.size) {
            this.removeView(textGrid[i])
        }
        textGrid.clear()

        for (i in 0 until strings.size) {

            textGrid.add(TextView(context))
            textGrid[i].setText(strings[i])
            textGrid[i].textSize = 18f
            textGrid[i].setTextColor(Color.BLACK)
            textGrid[i].textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            textGrid[i].minHeight = 30
            textGrid[i].id = View.generateViewId()
            textGrid[i].layoutParams = layoutParams
            this.addView(textGrid[i])

        }
    }

}