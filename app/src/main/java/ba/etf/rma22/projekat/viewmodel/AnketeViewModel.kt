package ba.etf.rma22.projekat.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.etf.rma21.projekat.data.AppDatabase
import ba.etf.rma22.projekat.data.models.Anketa
import ba.etf.rma22.projekat.data.models.AnketaTaken
import ba.etf.rma22.projekat.data.models.Odgovor
import ba.etf.rma22.projekat.data.models.Pitanje
import ba.etf.rma22.projekat.data.repositories.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnketeViewModel : ViewModel() {
    private lateinit var instancaBaze: AppDatabase
    private val anketaRepo = AnketaRepository.Companion
    private val pitanjeAnketaRepo = PitanjeAnketaRepository.Companion
    private val odgovoriRepo = OdgovorRepository.Companion
    private val pokusajiRepo = TakeAnketaRepository.Companion
    var trenutniPokusaj : AnketaTaken? = null
    var ankete = MutableLiveData<List<Anketa>>(emptyList())
    var pitanjaIOdgovori = MutableLiveData<List<Pair<Pitanje, Odgovor?>>>(emptyList())
    val hashUpdated = MutableLiveData<Boolean>()

    fun napraviInstancuBaze(context: Context) {
        instancaBaze = AppDatabase.getInstance(context)
        AccountRepository.db = instancaBaze
        AnketaRepository.db = instancaBaze
        IstrazivanjeIGrupaRepository.db = instancaBaze
        OdgovorRepository.db = instancaBaze
        TakeAnketaRepository.db = instancaBaze
        PitanjeAnketaRepository.db = instancaBaze
    }

    fun getAll() {
        viewModelScope.launch(Dispatchers.IO) {
            ankete.postValue(anketaRepo.getAll())
        }
    }

    fun getMyAnkete() {
        viewModelScope.launch(Dispatchers.IO) {
            ankete.postValue(anketaRepo.getUpisane())
        }
    }

    fun getDone() {
        viewModelScope.launch(Dispatchers.IO) {
            ankete.postValue(anketaRepo.getDone())
        }
    }

    fun getFuture() {
        viewModelScope.launch(Dispatchers.IO) {
            ankete.postValue(anketaRepo.getFuture())
        }
    }

    fun getNotTaken() {
        viewModelScope.launch(Dispatchers.IO) {
            ankete.postValue(anketaRepo.getNotTaken())
        }
    }
    //fun isUpisana(anketa: Anketa) = anketaRepo.getMyAnkete().contains(anketa) and pitanjaIOdgovori.value.isNotEmpty()

    fun getOdgovori(anketa: Anketa) = anketaRepo.getOdgovore(anketa)

    fun zapocniAnketu(anketa: Anketa) {
        viewModelScope.launch(Dispatchers.IO) {
            pokusajiRepo.zapocniAnketu(anketa.id)?.also { pokusaj ->
                trenutniPokusaj = pokusaj
                val pitanja = pitanjeAnketaRepo.getPitanja(anketa.id)
                val odgovori = odgovoriRepo.getOdgovoriAnketa(anketa.id)
                val lista: ArrayList<Pair<Pitanje, Odgovor?>> = arrayListOf()
                for (pitanje in pitanja) {
                    lista.add(Pair(pitanje, odgovori.firstOrNull { it.id == pitanje.id }))
                }
                pitanjaIOdgovori.postValue(lista)
            }
        }
    }

    fun postaviHash(hash: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!hash.isNullOrBlank()) {
                hashUpdated.postValue(AccountRepository.postaviHash(hash))
            }
        }
    }
}