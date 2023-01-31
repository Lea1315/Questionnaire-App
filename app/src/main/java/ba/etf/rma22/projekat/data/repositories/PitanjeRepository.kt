package ba.etf.rma22.projekat.data.repositories

import ba.etf.rma22.projekat.data.models.Pitanje

class PitanjeRepository {
    companion object {
        private var pitanja = arrayListOf<Pitanje>()

        fun getAll() : List<Pitanje> = pitanja
    }
}