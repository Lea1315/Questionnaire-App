package ba.etf.rma22.projekat.data.repositories

import ba.etf.rma22.projekat.data.models.Grupa

class GrupaRepository {
    companion object {
        private val sveGrupe = arrayListOf<Grupa>()
        private val upisaneGrupe = arrayListOf<Grupa>()

        fun getGroupsByIstrazivanje(nazivIstrazivanja: String): List<Grupa> {
            return sveGrupe.filter { grupa -> grupa.nazivIstrazivanja == nazivIstrazivanja }
        }

        fun upisiGrupu(grupa : Grupa) {
            sveGrupe.remove(grupa)
            upisaneGrupe.add(grupa)
        }
    }
}