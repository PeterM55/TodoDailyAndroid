package peter.mitchell.tododaily.HelperClasses

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import java.lang.reflect.Array

class ListOfTextGrids @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    var titleText : ArrayList<TextView> = ArrayList(10)
    private var sectionGrids : ArrayList<TextGridLayout> = ArrayList()
    var listContent : ArrayList<ArrayList<TextView>> = ArrayList(10)

    var sectionOpened : ArrayList<Boolean> = ArrayList()

    private val titleTextLayoutParams : ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
        ConstraintLayout.LayoutParams.WRAP_CONTENT, // width
        ConstraintLayout.LayoutParams.WRAP_CONTENT,
    )

    init {
        this.orientation = LinearLayout.VERTICAL
    }

    fun setupTitles(titles : ArrayList<String>) {

        for (i in 0 until titles.size) {
            var constraintLayout : ConstraintLayout = ConstraintLayout(context)
            var constraintSet : ConstraintSet = ConstraintSet()

            var sectionTitle : TextView = TextView(context)
            sectionTitle.setText(titles[i])
            sectionTitle.textSize = 18f
            sectionTitle.setTextColor(Color.BLACK)
            sectionTitle.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            sectionTitle.minHeight = 30
            sectionTitle.id = View.generateViewId()
            sectionTitle.layoutParams = layoutParams

            var sectionButton : Button = Button(context)
            sectionOpened.add(false)
            sectionButton.setText("◀")
            sectionButton.id = View.generateViewId()

            constraintSet.connect(
                sectionButton.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
            )

            this.addView(constraintLayout)

            var sectionGrid : TextGridLayout = TextGridLayout(context)
            this.addView(sectionGrid)

            sectionButton.setOnClickListener {
                if (sectionOpened[i]) {
                    sectionButton.setText("▼")
                    sectionGrid.isVisible = true
                } else {
                    sectionButton.setText("◀")
                    sectionGrid.isVisible = false
                }
            }

            // set global variables
            titleText.add(sectionTitle)
            sectionGrids.add(sectionGrid)
            listContent.add(ArrayList(10))
        }
    }

    fun setupContent(strings : ArrayList<ArrayList<String>>) : Boolean {
        if (titleText.size != strings.size) return false

        for (i in 0 until strings.size) {

            for (j in 0 until strings[i].size) {

                sectionGrids[i].addString(context, strings[i][j])
                listContent[i].add(sectionGrids[i].textGrid[j])

            }

        }

        return true
    }

}