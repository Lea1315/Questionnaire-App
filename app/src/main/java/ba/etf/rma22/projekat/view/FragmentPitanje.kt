package ba.etf.rma22.projekat.view

import android.content.Context
import android.graphics.Color.parseColor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import ba.etf.rma22.projekat.R
import ba.etf.rma22.projekat.data.models.Pitanje
import ba.etf.rma22.projekat.interfaces.PitanjeListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentPitanje.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentPitanje(val pitanje: Pitanje) : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: Int? = null
    private lateinit var handler: PitanjeListener
    private lateinit var lv: ListView
    private var odgovoreno = 0

    private val onItemClickListener = AdapterView.OnItemClickListener { parent, view, _, _ ->
        view as TextView
        view.setTextColor(parseColor("#0000FF"))
        odgovoreno = 1
        parent.children.forEachIndexed { index, child ->
            child as TextView
            if (view == child) pitanje.index = index
        }
        parent.isEnabled = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler = context as PitanjeListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pitanje, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tekstPitanja = view.findViewById<TextView>(R.id.tekstPitanja)
        tekstPitanja.text = pitanje.tekstPitanja
        val dugmeZaustavi = view.findViewById<Button>(R.id.dugmeZaustavi)
        dugmeZaustavi.setOnClickListener { handler.zaustaviAnketu() }
        lv = view.findViewById(R.id.odgovoriLista)
        lv.onItemClickListener = onItemClickListener
        val adapter = object : ArrayAdapter<String>(
            view.context,
            android.R.layout.simple_list_item_1,
            pitanje.opcije.split(',')
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val vju = super.getView(position, null, parent)
                param1?.let {
                    if (position == it) {
                        vju as TextView
                        vju.setTextColor(parseColor("#0000FF"))
                        odgovoreno = 1
                        lv.isEnabled = false
                        dugmeZaustavi.isVisible = false
                    }
                }
                return vju
            }
        }
        lv.adapter = adapter
    }

    fun isOdgovoreno() = odgovoreno
}