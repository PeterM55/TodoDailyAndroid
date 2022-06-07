package peter.mitchell.tododaily.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import peter.mitchell.tododaily.databinding.FragmentNotesBinding
import peter.mitchell.tododaily.mainBinding

class NotesFragment : Fragment() {

    private lateinit var _binding: FragmentNotesBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        mainBinding?.fragmentLabel?.setText("Notes")

        var testText : TextView = TextView(requireContext())
        testText.text = "This is a test"
        testText.textSize = 15f

        _binding.mainNotesLayout.addView(testText)

        return root
    }
}