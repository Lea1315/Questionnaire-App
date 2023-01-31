package ba.etf.rma22.projekat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import ba.etf.rma22.projekat.data.models.*
import ba.etf.rma22.projekat.data.repositories.AccountRepository
import ba.etf.rma22.projekat.data.repositories.AnketaRepository
import ba.etf.rma22.projekat.data.repositories.OdgovorRepository
import ba.etf.rma22.projekat.interfaces.OpenAnketaListener
import ba.etf.rma22.projekat.interfaces.PitanjeListener
import ba.etf.rma22.projekat.interfaces.PredajaListener
import ba.etf.rma22.projekat.interfaces.ShowMessageListener
import ba.etf.rma22.projekat.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), ShowMessageListener, OpenAnketaListener, PitanjeListener,
    PredajaListener {
    private lateinit var pager: ViewPager2
    private lateinit var adapter: ViewPagerAdapter
    private var aktivna: Anketa? = null
    private var aktivniPokusaj: AnketaTaken? = null
    private var payload: String = "b6c3bb54-0628-4eb7-aafd-6c0d37e2b147"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        intent?.let {
            payload = it.getStringExtra("payload") ?: "b6c3bb54-0628-4eb7-aafd-6c0d37e2b147"
        }
        pager = findViewById(R.id.pager)
        val fragments =
            mutableListOf(
                FragmentAnkete().apply {
                    arguments = bundleOf("acHash" to payload)
                },
                FragmentIstrazivanje()
            )
        adapter = ViewPagerAdapter(supportFragmentManager, fragments, lifecycle)
        pager.adapter = adapter
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 0) adapter.refreshFragment(1, FragmentIstrazivanje.newInstance())
            }
        })
    }

    override fun ubaciPoruku(grupa: Grupa?) {
        adapter.refreshFragment(
            1,
            FragmentPoruka.newInstance(param1 = "Uspješno ste upisani u grupu ${grupa!!.naziv} istraživanja ${grupa.nazivIstrazivanja}!")
        )
        (adapter.fragments[0] as FragmentAnkete).refreshAnkete()
    }


    override fun otvoriAnketu(anketa: Anketa, pokusaj: AnketaTaken?, lista: List<Pair<Pitanje, Odgovor?>>) {
        aktivna = anketa
        aktivniPokusaj = pokusaj
        adapter.removeAll()
        val fragmenti = arrayListOf<Fragment>()
        lista.forEach { par ->
            fragmenti.add(FragmentPitanje(par.first).apply {
                par.second?.let {
                    val bundle = Bundle()
                    bundle.putInt("param1", it.odgovoreno)
                    arguments = bundle
                }
            })
        }
        adapter.addBulk(fragmenti)
        adapter.add(fragmenti.size, FragmentPredaj())
    }

    override fun zaustaviAnketu() {
        lifecycleScope.launch(Dispatchers.IO) {
            var brojac = 0
            adapter.fragments.filterIsInstance<FragmentPitanje>().map { it.pitanje }.forEach {
                if (it.index != -1) {
                    OdgovorRepository.postaviOdgovorAnketa(aktivniPokusaj!!.id, it.id, it.index).also { rez ->
                        if (rez == -1) brojac++
                    }
                }
            }
            withContext(Dispatchers.Main) {
                if (brojac != 0) Toast.makeText(baseContext, "Doslo je do greske prilikom spasavanja odgovora", Toast.LENGTH_LONG).show()
                adapter.removeAll()
                adapter.addBulk(listOf(FragmentAnkete(), FragmentIstrazivanje()))
                pager.currentItem = 0
            }
        }
    }

    override fun predajAnketu() {
        lifecycleScope.launch(Dispatchers.IO) {
            var brojac = 0
            adapter.fragments.filterIsInstance<FragmentPitanje>().map { it.pitanje }.forEach {
                if (it.index != -1) {
                    OdgovorRepository.postaviOdgovorAnketa(aktivniPokusaj!!.id, it.id, it.index).also { rez ->
                        if (rez == -1) brojac++
                    }
                }
            }
            withContext(Dispatchers.Main) {
                if (brojac != 0) Toast.makeText(baseContext, "Doslo je do greske prilikom spasavanja odgovora", Toast.LENGTH_LONG).show()
                adapter.removeAll()
                adapter.addBulk(
                    listOf(
                        FragmentAnkete(),
                        FragmentPoruka.newInstance(
                            param1 = "Završili ste anketu ${aktivna!!.naziv} u okviru istraživanja ${aktivna!!.nazivIstrazivanja}"
                        )
                    )
                )
                pager.currentItem = 1
            }
        }
    }

    override fun updateProgres(): String {
        return aktivniPokusaj!!.progres.toString()
    }
}