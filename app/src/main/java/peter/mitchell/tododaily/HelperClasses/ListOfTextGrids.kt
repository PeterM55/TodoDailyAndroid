package peter.mitchell.tododaily.HelperClasses

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import peter.mitchell.tododaily.R
import peter.mitchell.tododaily.darkMode

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
    var sectionsColumnCount = 2
    var customTextSize = 18f

    private var showAddButtons = true

    private val titleTextLayoutParams : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
        resources.displayMetrics.widthPixels-62*2-34, // width
        RelativeLayout.LayoutParams.WRAP_CONTENT,
    )
    private val titleButtonLayoutParams : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
        (55*resources.displayMetrics.density).toInt(), // width
        (40*resources.displayMetrics.density).toInt(),
    )
    private val addButtonLayoutParams : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
        (55*resources.displayMetrics.density).toInt(), // width
        (40*resources.displayMetrics.density).toInt(),
    )
    private val constraintLayoutParams : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT, // width
        (40*resources.displayMetrics.density).toInt(),
    )

    init {
        this.orientation = LinearLayout.VERTICAL

        titleTextLayoutParams.addRule(RelativeLayout.ALIGN_LEFT)
        titleButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
    }

    /** Removes all grids and buttons */
    private fun clearSetup() {
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

    /** Clears the setup, and adds the listed titles
     * @param titles the arraylist of titles to be used
     */
    fun setupTitles(titles : ArrayList<String>) {

        clearSetup()

        for (i in 0 until titles.size) {
            val relativeLayout : RelativeLayout = RelativeLayout(context)
            relativeLayout.id = View.generateViewId()

            constraintLayoutParams.topMargin = (10*resources.displayMetrics.density).toInt()
            relativeLayout.layoutParams = constraintLayoutParams

            val sectionTitle : TextView = TextView(context)
            sectionTitle.setText(titles[i])
            sectionTitle.textSize = 24f
            sectionTitle.setTypeface(null, Typeface.BOLD)
            if (darkMode)
                sectionTitle.setTextColor(resources.getColor(R.color.textDark))
            else
                sectionTitle.setTextColor(resources.getColor(R.color.textLight))
            sectionTitle.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            sectionTitle.minHeight = 30
            sectionTitle.setTypeface(null, Typeface.BOLD)
            sectionTitle.id = View.generateViewId()
            val titleTextLayoutParamsCopy : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // width
                RelativeLayout.LayoutParams.WRAP_CONTENT,
            )
            sectionTitle.layoutParams = titleTextLayoutParamsCopy

            val sectionAddButton : Button = Button(context)
            sectionAddButton.setText("+")
            sectionAddButton.layoutParams = titleButtonLayoutParams
            sectionAddButton.id = View.generateViewId()

            val sectionButton : Button = Button(context)
            sectionOpened.add(true)
            sectionButton.setText("▼")
            sectionButton.layoutParams = titleButtonLayoutParams
            sectionButton.id = View.generateViewId()

            sectionAddButton.minHeight = 0
            sectionButton.minHeight = 0
            sectionAddButton.minWidth = 0
            sectionButton.minWidth = 0

            val tempLayout : RelativeLayout.LayoutParams = addButtonLayoutParams
            tempLayout.addRule(RelativeLayout.LEFT_OF, sectionButton.id)
            sectionAddButton.layoutParams = tempLayout

            relativeLayout.addView(sectionTitle)
            relativeLayout.addView(sectionButton)
            relativeLayout.addView(sectionAddButton)

            this.addView(relativeLayout)

            val sectionGrid : TextGridLayout = TextGridLayout(context)
            sectionGrid.setCustomColumnCount(sectionsColumnCount)
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
                val tempLayout2 : RelativeLayout.LayoutParams = addButtonLayoutParams
                tempLayout2.addRule(RelativeLayout.LEFT_OF, sectionButton.id)
            }

            // set global variables
            relativeLayouts.add(relativeLayout)
            titleText.add(sectionTitle)
            sectionButtons.add(sectionButton)
            sectionAddButtons.add(sectionAddButton)
            sectionGrids.add(sectionGrid)
            listContent.add(ArrayList(10))
            sectionTitle.layoutParams = titleTextLayoutParams
        }
    }

    /** Setup the content of the text grids
     * @param strings arraylist of arraylists of strings, inner arraylists are put in each grid
     * @return whether it worked
     */
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
     * @param i the index
     * @param visible whether the section should be visible
     */
    public fun setSectionVisible(i : Int, visible : Boolean) {
        relativeLayouts[i].isVisible = visible
        sectionGrids[i].isVisible = visible
    }

    /** Set whether the add buttons should be shown for each section
     * @param visible whether they should be visible
     */
    fun setShowAddButtons(visible : Boolean) {
        showAddButtons = visible
        for (i in 0 until sectionAddButtons.size)
            sectionAddButtons[i].isVisible = showAddButtons
    }

    /** Sets the column count for the grids
     * @param count The column count
     */
    fun setCustomColumnCount(count : Int) {
        sectionsColumnCount = count
        for (i in 0 until sectionGrids.size) {
            sectionGrids[i].setCustomColumnCount(count)
        }
        columnCount = count
    }

    /** Set the text size of the content of the grids
     * @param size The text size
     */
    fun setTextSize(size : Float) {
        for (i in 0 until sectionGrids.size) {
            sectionGrids[i].setTextSize(size)
        }
        customTextSize = size
    }

}