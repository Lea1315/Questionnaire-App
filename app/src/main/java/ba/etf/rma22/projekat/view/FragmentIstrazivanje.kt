package ba.etf.rma22.projekat.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import ba.etf.rma22.projekat.R
import ba.etf.rma22.projekat.data.models.Grupa
import ba.etf.rma22.projekat.interfaces.ShowMessageListener
import ba.etf.rma22.projekat.viewmodel.UpisViewModel

class FragmentIstrazivanje : Fragment() {
    private val viewmodel = UpisViewModel()
    private lateinit var adapterGodine : ArrayAdapter<CharSequence>
    private lateinit var adapterIstrazivanja : ArrayAdapter<String>
    private lateinit var adapterGrupa : ArrayAdapter<String>
    private lateinit var handler: ShowMessageListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler = context as ShowMessageListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_istrazivanje, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //super.onViewCreated(view, savedInstanceState)
        val spinnerGodine = view.findViewById<Spinner>(R.id.odabirGodina)
        val spinnerIstrazivanja = view.findViewById<Spinner>(R.id.odabirIstrazivanja)
        val spinnerGrupe = view.findViewById<Spinner>(R.id.odabirGrupa)
        val dugme = view.findViewById<Button>(R.id.dodajIstrazivanjeDugme)
        adapterGodine = ArrayAdapter.createFromResource(requireContext(), R.array.godine, android.R.layout.simple_spinner_dropdown_item)
        adapterIstrazivanja = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, arrayListOf())
        adapterGrupa = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, arrayListOf())
        viewmodel.istrazivanja.observe(viewLifecycleOwner) { lista ->
            adapterIstrazivanja.clear()
            adapterIstrazivanja.addAll(lista.map { it.naziv })
            adapterIstrazivanja.notifyDataSetChanged()
            adapterGrupa.notifyDataSetChanged()
        }

        viewmodel.grupe.observe(viewLifecycleOwner) { lista ->
            adapterGrupa.clear()
            adapterGrupa.addAll(lista.map { it.naziv })
            adapterGrupa.notifyDataSetChanged()
            dugme.isVisible = !adapterGrupa.isEmpty
        }

        viewmodel.upisUspjesan.observe(viewLifecycleOwner) {
            if (it) {
                handler.ubaciPoruku(Grupa(1,spinnerGrupe.selectedItem.toString(), spinnerIstrazivanja.selectedItem.toString()))
                (spinnerGodine.onItemSelectedListener as AdapterView.OnItemSelectedListener)
                    .onItemSelected(null, null, spinnerGodine.selectedItemPosition, 1L)
            } else {
                Toast.makeText(requireContext(), "Internet konekcija nije dostupna", Toast.LENGTH_LONG).show()
            }
        }

        spinnerGodine.adapter = adapterGodine
        spinnerIstrazivanja.adapter = adapterIstrazivanja
        spinnerGrupe.adapter = adapterGrupa
        spinnerGodine.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, trenutni: View?, position: Int, p3: Long) {
                viewmodel.getNeupisanaIstrazivanja(position + 1)
                spinnerIstrazivanja.onItemSelectedListener?.onItemSelected(p0, trenutni, 0, p3)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        spinnerIstrazivanja.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, trenutni: View?, position: Int, p3: Long) {
                if(spinnerIstrazivanja.selectedItem == null) spinnerIstrazivanja.setSelection(0)
                viewmodel.getGrupeZaIstrazivanje(spinnerIstrazivanja.selectedItem?.toString())
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        dugme.setOnClickListener {
            viewmodel.upisiKorisnika(spinnerIstrazivanja.selectedItem.toString(), spinnerGrupe.selectedItem.toString())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FragmentIstrazivanje()
    }
}