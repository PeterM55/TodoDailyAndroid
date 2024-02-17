package peter.mitchell.tododaily.ui.notes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.work.WorkManager
import peter.mitchell.tododaily.*
import peter.mitchell.tododaily.HelperClasses.NotesList
import peter.mitchell.tododaily.databinding.FragmentNotesBinding
import peter.mitchell.tododaily.ui.notifications.NotificationsFragmentDirections

/** The fragment for basic notes functionality, showing the list of titles of the notes and lists
 * editing and moving the notes is handled by EditNotes and ManageNotes respectively
 */
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

        if (darkMode)
            _binding.mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundDark))
        else
            _binding.mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundLight))


        _binding.manageNotesButton.setOnClickListener {
            val intent = Intent(requireContext(), ManageNotes::class.java)
            startActivity(intent)
        }

        return root
    }

    /** refreshes the notes list, using the information from notesList.notesFiles and
     * notesList.listsFiles
     */
    fun refreshNotes() {
        var titles : ArrayList<String> = ArrayList()
        titles.add("Notes")
        titles.add("Lists")
        _binding.notesListGrid.setupTitles(titles)

        _binding.notesListGrid.sectionGrids[0].setCustomColumnCount(notesColumns)
        _binding.notesListGrid.sectionGrids[0].setTextSize(notesTextSize)
        _binding.notesListGrid.sectionGrids[1].setCustomColumnCount(listsColumns)
        _binding.notesListGrid.sectionGrids[1].setTextSize(listsTextSize)

        var content : ArrayList<ArrayList<String>> = ArrayList(2)
        content.add(notesList!!.notesFiles)
        content.add(notesList!!.listsFiles)
        _binding.notesListGrid.setupContent(content)

        _binding.notesListGrid.sectionAddButtons[0].setOnClickListener {
            val intent = Intent(requireContext(), EditNotes::class.java)
            intent.putExtra("isList", false)
            intent.putExtra("selectedFileIndex", -1)
            startActivity(intent)
        }
        _binding.notesListGrid.sectionAddButtons[1].setOnClickListener {
            val intent = Intent(requireContext(), EditNotes::class.java)
            intent.putExtra("isList", true)
            intent.putExtra("selectedFileIndex", -1)
            startActivity(intent)
        }

        for (i in notesList!!.notesFiles.indices) {
            _binding.notesListGrid.listContent[0][i].setOnClickListener {
                val intent = Intent(requireContext(), EditNotes::class.java)
                intent.putExtra("isList", false)
                intent.putExtra("selectedFileIndex", i)
                startActivity(intent)
            }
        }

        for (i in notesList!!.listsFiles.indices) {
            _binding.notesListGrid.listContent[1][i].setOnClickListener {
                val intent = Intent(requireContext(), EditNotes::class.java)
                intent.putExtra("isList", true)
                intent.putExtra("selectedFileIndex", i)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (darkMode)
            _binding.mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundDark))
        else
            _binding.mainBackground.setBackgroundColor(resources.getColor(peter.mitchell.tododaily.R.color.backgroundLight))

        if (!notesShown) {
            val action = NotesFragmentDirections.actionNavigationNotesToNavigationHome()
            view?.findNavController()?.navigate(action)
        }

        notesList = NotesList()
        refreshNotes()

        updateBottomNavVisibilities()
    }
}