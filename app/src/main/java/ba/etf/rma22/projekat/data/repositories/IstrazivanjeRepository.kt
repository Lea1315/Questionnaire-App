package ba.etf.rma22.projekat.data.repositories

import ba.etf.rma22.projekat.data.models.Istrazivanje

class IstrazivanjeRepository {
    companion object {
        private val neupisanaIstrazivanja = arrayListOf<Istrazivanje>()
        private val upisanaIstrazivanja = arrayListOf<Istrazivanje>()


        fun getIstrazivanjeByGodina(year: Int) : List<Istrazivanje> {
            return getAll().filter { istr -> istr.godina == year }
        }

        fun getUpisani(): List<Istrazivanje> = upisanaIstrazivanja

        fun getAll(): List<Istrazivanje> = neupisanaIstrazivanja + upisanaIstrazivanja

        fun upisiIstrazivanje(istrazivanje: Istrazivanje) {
            neupisanaIstrazivanja.remove(istrazivanje)
            upisanaIstrazivanja.add(istrazivanje)
        }
    }
}