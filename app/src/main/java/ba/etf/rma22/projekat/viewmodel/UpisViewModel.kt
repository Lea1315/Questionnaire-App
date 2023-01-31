package ba.etf.rma22.projekat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.etf.rma22.projekat.data.models.Grupa
import ba.etf.rma22.projekat.data.models.Istrazivanje
import ba.etf.rma22.projekat.data.repositories.IstrazivanjeIGrupaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpisViewModel: ViewModel() {
    private val istrazivanjeIGrupaRepo = IstrazivanjeIGrupaRepository.Companion
    val istrazivanja = MutableLiveData<List<Istrazivanje>>(emptyList())
    val grupe = MutableLiveData<List<Grupa>>(emptyList())
    val upisUspjesan = MutableLiveData<Boolean>()

    fun getNeupisanaIstrazivanja(godina : Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val istrazivanja =
                istrazivanjeIGrupaRepo.getIstrazivanja().filter { it.godina == godina }
            val upisanaIstrazivanja =
                istrazivanjeIGrupaRepo.getUpisaneGrupe().map { it.nazivIstrazivanja }
            this@UpisViewModel.istrazivanja.postValue(istrazivanja.filter { !upisanaIstrazivanja.contains(it.naziv) })
        }
    }

    fun getGrupeZaIstrazivanje(istrazivanje: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (istrazivanje != null) {
                val idIstrazivanja = istrazivanja.value?.first { it.naziv == istrazivanje }?.id
                idIstrazivanja?.let {
                    grupe.postValue(istrazivanjeIGrupaRepo.getGrupeZaIstrazivanje(idIstrazivanja))
                }
            } else grupe.postValue(emptyList())
        }
    }

    fun upisiKorisnika(istrazivanje: String, grupa: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val trazenaGrupa = grupe.value?.first { it.naziv == grupa && it.nazivIstrazivanja == istrazivanje }
            trazenaGrupa?.let {
                upisUspjesan.postValue(istrazivanjeIGrupaRepo.upisiUGrupu(it.id))
            }
        }
    }
}