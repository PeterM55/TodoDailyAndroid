package peter.mitchell.tododaily.ui.dashboard

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.TextView
import androidx.fragment.app.Fragment
import peter.mitchell.tododaily.HelperClasses.TodoLists
import peter.mitchell.tododaily.databinding.FragmentDashboardBinding
import peter.mitchell.tododaily.saveInformation
import peter.mitchell.tododaily.todoLists

class DashboardFragment : Fragment() {

    private lateinit var _binding: FragmentDashboardBinding

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (todoLists == null) todoLists = TodoLists()

        var todoSectionTitles : ArrayList<TextView> = ArrayList(5)
        var todoSections : ArrayList<GridView> = ArrayList(5)


        for (i in 0 until todoLists!!.getSize()) {
            var sectionTitle : TextView = TextView(requireContext())
            sectionTitle.text = todoLists!!.getSectionTitle(i)
            sectionTitle.textSize = 20f
            todoSectionTitles.add(sectionTitle)

            var sectionGrid : GridView = GridView(requireContext())
            sectionGrid.adapter = ArrayAdapter(
                requireContext(),
                R.layout.simple_list_item_1,
                todoLists!!.getTodo(i)
            )
            todoSections.add(sectionGrid)
        }
        //todo ^

        var testText : TextView = TextView(requireContext())
        testText.text = "This is a test"
        testText.textSize = 15f

        _binding.mainTodoLayout.addView(testText)


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}