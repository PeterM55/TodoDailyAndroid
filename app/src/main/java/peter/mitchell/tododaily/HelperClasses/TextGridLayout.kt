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
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.children
import kotlinx.android.synthetic.main.fragment_home.*
import org.w3c.dom.Text
import org.xmlpull.v1.XmlPullParser
import peter.mitchell.tododaily.R
import peter.mitchell.tododaily.dailyNotifications
import peter.mitchell.tododaily.darkMode
import peter.mitchell.tododaily.saveInformation

/** A grid of text views, also used by listOfTextGrids to create an array of these
 * This was created because gridView could not do all of the functions needed properly
 */
class TextGridLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : GridLayout(context, attrs) {

    public var textGrid : ArrayList<TextView> = ArrayList<TextView>(25)
    var customTextSize : Float = 18f

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

    /** Reset the view, removing all elements (but not resetting text size or column count) */
    public fun reset() {
        for (i in 0 until textGrid.size) {
            this.removeView(textGrid[i])
        }
        textGrid.clear()
    }

    /** Add the string to the list
     *
     * @param context used to create the text view
     * @param str the string to add
     */
    public fun addString(context : Context, str : String) {
        textGrid.add(TextView(context))
        textGrid[textGrid.size-1].setText(str)
        textGrid[textGrid.size-1].textSize = customTextSize
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

    /** sets the strings to be the contents of the arraylist
     *
     * @param context used to create the text views
     * @param strings the array of strings
     */
    public fun setAdapter(context : Context, strings : ArrayList<String>) {
        reset()

        for (i in 0 until strings.size) {

            textGrid.add(TextView(context))
            textGrid[i].setText(strings[i])
            textGrid[i].textSize = customTextSize
            textGrid[i].setTextColor(Color.BLACK)
            textGrid[i].textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            textGrid[i].minHeight = 30
            textGrid[i].id = View.generateViewId()
            textGrid[i].layoutParams = textLayoutParams

            if (darkMode)
                textGrid[textGrid.size-1].setTextColor(resources.getColor(R.color.textDark))
            else
                textGrid[textGrid.size-1].setTextColor(resources.getColor(R.color.textLight))

            this.addView(textGrid[i])

        }
    }

    /** Set the column count of the grid view
     *
     * @param count the number of columns
     */
    fun setCustomColumnCount(count : Int, padding : Int = 8) {
        if (count == columnCount) return

        textLayoutParams.width = resources.displayMetrics.widthPixels/count-padding

        var tempTextArrayList : ArrayList<String> = ArrayList(textGrid.size)
        for (i in 0 until textGrid.size) {
            tempTextArrayList.add(textGrid[i].text.toString())
        }

        reset()
        // before column count can be set to anything smaller, the contents must be reset for some reason.
        columnCount = count

        setAdapter(context, tempTextArrayList)
    }

    /** Set the text size of the grid view
     *
     * @param size the text size
     */
    fun setTextSize(size : Float) {
        for (i in 0 until textGrid.size) {
            textGrid[i].textSize = size
        }
        customTextSize = size
    }


}