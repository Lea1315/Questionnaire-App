package ba.etf.rma22.projekat.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ba.etf.rma22.projekat.R
import ba.etf.rma22.projekat.data.models.Anketa
import ba.etf.rma22.projekat.interfaces.OpenAnketaListener
import ba.etf.rma22.projekat.viewmodel.AnketeViewModel
import java.util.*

class FragmentAnkete : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AnketaAdapter
    private var anketeViewModel = AnketeViewModel()
    private lateinit var spinner: Spinner
    private var zadnjaGodina: Int = 0
    private var trenutnaAnketa : Anketa? = null

    private lateinit var anketeListener: OpenAnketaListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ankete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.listaAnketa)
        anketeViewModel.napraviInstancuBaze(requireActivity().applicationContext)
        anketeViewModel.postaviHash(arguments?.getString("acHash"))
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = AnketaAdapter(
            Collections.emptyList(),
            requireContext()
        ) { anketa ->
            trenutnaAnketa = anketa
            anketeViewModel.zapocniAnketu(anketa)
        }
        recyclerView.adapter = adapter
        anketeViewModel.ankete.observe(viewLifecycleOwner) { lista ->
            adapter.updateAnkete(lista)
        }
        anketeViewModel.pitanjaIOdgovori.observe(viewLifecycleOwner) { lista ->
            trenutnaAnketa?.let {
                anketeListener.otvoriAnketu(it, anketeViewModel.trenutniPokusaj, lista)
            }
        }
        anketeViewModel.hashUpdated.observe(viewLifecycleOwner) {
            anketeViewModel.getMyAnkete()
        }
        spinner = view.findViewById(R.id.filterAnketa)
        val arrayAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ankete,
            android.R.layout.simple_spinner_dropdown_item
        )

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> anketeViewModel.getMyAnkete()
                    1 -> anketeViewModel.getAll()
                    2 -> anketeViewModel.getDone()
                    3 -> anketeViewModel.getFuture()
                    4 -> anketeViewModel.getNotTaken()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        anketeListener = context as OpenAnketaListener
    }

    fun getAdapter() = adapter
    fun refreshAnkete() {
        anketeViewModel.getMyAnkete()
    }


    companion object {
        @JvmStatic
        fun newInstance() = FragmentAnkete()
    }
}