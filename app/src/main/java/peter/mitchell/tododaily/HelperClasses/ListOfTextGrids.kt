package peter.mitchell.tododaily.HelperClasses

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.textclassifier.TextClassifier
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import peter.mitchell.tododaily.R
import peter.mitchell.tododaily.darkMode
import java.lang.reflect.Array

class ListOfTextGrids @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    var relativeLayouts : ArrayList<RelativeLayout> = ArrayList()

    var titleText : ArrayList<TextView> = ArrayList(10)
    var sectionAddButtons : ArrayList<Button> = ArrayList()
    private var sectionButtons : ArrayList<Button> = ArrayList()
    var sectionGrids : ArrayList<TextGridLayout> = ArrayList()
    var listContent : ArrayList<ArrayList<TextView>> = ArrayList(10)

    var sectionOpened : ArrayList<Boolean> = ArrayList()

    var columnCount = 2
    var customTextSize = 18f

    private val titleTextLayoutParams : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
        resources.displayMetrics.widthPixels-62*2-34, // width //RelativeLayout.LayoutParams.WRAP_CONTENT
        RelativeLayout.LayoutParams.WRAP_CONTENT,
    )
    private val titleButtonLayoutParams : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
        (55*resources.displayMetrics.density).toInt(), // width (resources.displayMetrics.density)
        (40*resources.displayMetrics.density).toInt(),
    )
    private val addButtonLayoutParams : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
        (55*resources.displayMetrics.density).toInt(), // width
        (40*resources.displayMetrics.density).toInt(),
    )
    private val constraintLayoutParams : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT, // width
        (40*resources.displayMetrics.density).toInt(), //FrameLayout.LayoutParams.WRAP_CONTENT
    )

    init {
        this.orientation = LinearLayout.VERTICAL

        titleTextLayoutParams.addRule(RelativeLayout.ALIGN_LEFT)
        titleButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
    }

    fun clearSetup() {
        for (i in 0 until listContent.size) {
            this.removeView(relativeLayouts[i])
            this.removeView(sectionGrids[i])
        }

        relativeLayouts.clear()
        titleText.clear()
        sectionAddButtons.clear()
        sectionButtons.clear()
        sectionGrids.clear()
        listContent.clear()
    }

    fun setupTitles(titles : ArrayList<String>) {

        clearSetup()

        for (i in 0 until titles.size) {
            var constraintLayout : RelativeLayout = RelativeLayout(context)
            constraintLayout.id = View.generateViewId()

            constraintLayout.layoutParams = constraintLayoutParams
            var constraintSet : ConstraintSet = ConstraintSet()

            var sectionTitle : TextView = TextView(context)
            sectionTitle.setText(titles[i])
            sectionTitle.textSize = 24f
            sectionTitle.setTypeface(null, Typeface.BOLD)
            if (darkMode)
                sectionTitle.setTextColor(resources.getColor(R.color.textDark))
            else
                sectionTitle.setTextColor(resources.getColor(R.color.textLight))
            sectionTitle.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            sectionTitle.minHeight = 30
            sectionTitle.id = View.generateViewId()
            val titleTextLayoutParamsCopy : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // width
                RelativeLayout.LayoutParams.WRAP_CONTENT,
            )
            sectionTitle.layoutParams = titleTextLayoutParamsCopy

            var sectionAddButton : Button = Button(context)
            sectionAddButton.setText("+")
            sectionAddButton.layoutParams = titleButtonLayoutParams
            sectionAddButton.id = View.generateViewId()

            var sectionButton : Button = Button(context)
            sectionOpened.add(true)
            sectionButton.setText("▼")
            sectionButton.layoutParams = titleButtonLayoutParams
            sectionButton.id = View.generateViewId()

            sectionAddButton.minHeight = 0
            sectionButton.minHeight = 0
            sectionAddButton.minWidth = 0
            sectionButton.minWidth = 0

            var tempLayout : RelativeLayout.LayoutParams = addButtonLayoutParams
            tempLayout.addRule(RelativeLayout.LEFT_OF, sectionButton.id)
           // tempLayout.addRule(RelativeLayout.RIGHT_OF, sectionTitle.id)
            sectionAddButton.layoutParams = tempLayout

            /*constraintSet.clone(constraintLayout)

            constraintSet.constrainHeight(sectionTitle.id, ConstraintSet.WRAP_CONTENT)
            constraintSet.constrainWidth(sectionTitle.id, ConstraintSet.WRAP_CONTENT)
            constraintSet.connect(sectionTitle.id,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,0)
            constraintSet.connect(sectionTitle.id,ConstraintSet.LEFT,ConstraintSet.PARENT_ID,ConstraintSet.LEFT,0)
            //constraintSet.connect(sectionTitle.id,ConstraintSet.RIGHT,sectionAddButton.id,ConstraintSet.LEFT,0)
            constraintSet.connect(sectionTitle.id,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,0)

            constraintSet.constrainHeight(sectionButton.id, ConstraintSet.WRAP_CONTENT)
            constraintSet.constrainWidth(sectionButton.id, ConstraintSet.WRAP_CONTENT)
            constraintSet.connect(sectionButton.id,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,0)
            constraintSet.connect(sectionButton.id,ConstraintSet.LEFT,sectionAddButton.id,ConstraintSet.RIGHT,0)
            constraintSet.connect(sectionButton.id,ConstraintSet.RIGHT,ConstraintSet.PARENT_ID,ConstraintSet.RIGHT,0)
            constraintSet.connect(sectionButton.id,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,0)

            constraintSet.constrainHeight(sectionAddButton.id, ConstraintSet.WRAP_CONTENT)
            constraintSet.constrainWidth(sectionAddButton.id, ConstraintSet.WRAP_CONTENT)
            constraintSet.connect(sectionAddButton.id,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,0)
            //constraintSet.connect(sectionButton.id,ConstraintSet.LEFT,sectionTitle.id,ConstraintSet.RIGHT,0)
            //constraintSet.connect(sectionAddButton.id,ConstraintSet.RIGHT,ConstraintSet.PARENT_ID,ConstraintSet.RIGHT,0)
            constraintSet.connect(sectionAddButton.id,ConstraintSet.RIGHT,sectionButton.id,ConstraintSet.LEFT,0)
            constraintSet.connect(sectionAddButton.id,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,0)

            constraintSet.constrainHeight(constraintLayout.id, ConstraintSet.MATCH_CONSTRAINT)
            constraintSet.constrainWidth(constraintLayout.id, ConstraintSet.WRAP_CONTENT)*/


            constraintLayout.addView(sectionTitle)
            constraintLayout.addView(sectionButton)
            constraintLayout.addView(sectionAddButton)
            //constraintSet.applyTo(constraintLayout)

            this.addView(constraintLayout)

            var sectionGrid : TextGridLayout = TextGridLayout(context)
            sectionGrid.setCustomColumnCount(3)
            //sectionGrid.
            this.addView(sectionGrid)

            sectionButton.setOnClickListener {
                sectionOpened[i] = !sectionOpened[i]
                if (sectionOpened[i]) {
                    sectionButton.setText("▼")
                    sectionGrid.isVisible = true
                } else {
                    sectionButton.setText("◀")
                    sectionGrid.isVisible = false
                }

                // for some reason the layout resets every time the button is hit, so has to be reset here
                var tempLayout2 : RelativeLayout.LayoutParams = addButtonLayoutParams
                tempLayout2.addRule(RelativeLayout.LEFT_OF, sectionButton.id)
                //tempLayout2.addRule(RelativeLayout.RIGHT_OF, sectionTitle.id)
                randomRefresh()
            }

            // set global variables
            relativeLayouts.add(constraintLayout)
            titleText.add(sectionTitle)
            sectionButtons.add(sectionButton)
            sectionAddButtons.add(sectionAddButton)
            sectionGrids.add(sectionGrid)
            listContent.add(ArrayList(10))
            sectionTitle.layoutParams = titleTextLayoutParams
        }
    }

    fun setupContent(strings : ArrayList<ArrayList<String>>) : Boolean {
        if (titleText.size != strings.size) return false

        for (i in 0 until strings.size) {

            sectionGrids[i].reset()

            for (j in 0 until strings[i].size) {

                sectionGrids[i].addString(context, strings[i][j])
                listContent[i].add(sectionGrids[i].textGrid[j])

            }

        }

        return true
    }

    /**
     * I have no idea why it breaks unless I call this.
     */
    fun randomRefresh() {
        for (i in 0 until sectionButtons.size) {
            /*// for some reason the layout resets every time the button is hit, so has to be reset here
            var tempLayout2: RelativeLayout.LayoutParams = addButtonLayoutParams
            tempLayout2.addRule(RelativeLayout.LEFT_OF, sectionButtons[i].id)*/

            //titleText[i].layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT
            //titleText[i].
        }
    }

    fun setCustomColumnCount(count : Int) {
        for (i in 0 until sectionGrids.size) {
            sectionGrids[i].setCustomColumnCount(count)
        }
        columnCount = count
    }

    fun setTextSize(size : Float) {
        for (i in 0 until sectionGrids.size) {
            sectionGrids[i].setTextSize(size)
        }
        customTextSize = size
    }

}