package ba.etf.rma22.projekat.interfaces

import ba.etf.rma22.projekat.data.models.Anketa
import ba.etf.rma22.projekat.data.models.AnketaTaken
import ba.etf.rma22.projekat.data.models.Odgovor
import ba.etf.rma22.projekat.data.models.Pitanje

interface OpenAnketaListener {
    fun otvoriAnketu(anketa : Anketa, pokusaj: AnketaTaken?, lista : List<Pair<Pitanje, Odgovor?>>)
}