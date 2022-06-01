package peter.mitchell.tododaily.HelperClasses

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.XmlResourceParser
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import kotlinx.android.synthetic.main.fragment_home.*
import org.w3c.dom.Text
import org.xmlpull.v1.XmlPullParser
import peter.mitchell.tododaily.R
import peter.mitchell.tododaily.dailyNotifications
import peter.mitchell.tododaily.darkMode
import peter.mitchell.tododaily.saveInformation

class TextGridLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : GridLayout(context, attrs) {

    public var textGrid : ArrayList<TextView> = ArrayList<TextView>(25)

    private val textLayoutParams : ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
        resources.displayMetrics.widthPixels/2-8, // width
        ConstraintLayout.LayoutParams.WRAP_CONTENT,
    )

    init {

        // setup layoutParams
        textLayoutParams.topMargin = 5
        textLayoutParams.leftMargin = 3
        textLayoutParams.bottomMargin = 20

    }

    public fun reset() {
        for (i in 0 until textGrid.size) {
            this.removeView(textGrid[i])
        }
        textGrid.clear()
    }

    public fun addString(context : Context, str : String) {
        textGrid.add(TextView(context))
        textGrid[textGrid.size-1].setText(str)
        textGrid[textGrid.size-1].textSize = 18f
        textGrid[textGrid.size-1].setTextColor(Color.BLACK)
        textGrid[textGrid.size-1].textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
        textGrid[textGrid.size-1].minHeight = 30
        textGrid[textGrid.size-1].id = View.generateViewId()
        textGrid[textGrid.size-1].layoutParams = textLayoutParams

        if (darkMode)
            textGrid[textGrid.size-1].setTextColor(resources.getColor(R.color.textDark))
        else
            textGrid[textGrid.size-1].setTextColor(resources.getColor(R.color.textLight))

        this.addView(textGrid[textGrid.size-1])
    }

    public fun setAdapter(context : Context, strings : ArrayList<String>) {
        reset()

        for (i in 0 until strings.size) {

            textGrid.add(TextView(context))
            textGrid[i].setText(strings[i])
            textGrid[i].textSize = 18f
            textGrid[i].setTextColor(Color.BLACK)
            textGrid[i].textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            textGrid[i].minHeight = 30
            textGrid[i].id = View.generateViewId()
            textGrid[i].layoutParams = layoutParams

            if (darkMode)
                textGrid[textGrid.size-1].setTextColor(resources.getColor(R.color.textDark))
            else
                textGrid[textGrid.size-1].setTextColor(resources.getColor(R.color.textLight))

            this.addView(textGrid[i])

        }
    }

    fun setCustomColumnCount(count : Int) {
        columnCount = count
        textLayoutParams.width = resources.displayMetrics.widthPixels/columnCount-8
    }

}