package ba.etf.rma22.projekat.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
data class Odgovor(
    @PrimaryKey
    val id: Int,
    val odgovoreno: Int,
    val idAnkete: Int
    )

fun JSONObject.parseOdgovor(id: Int): Odgovor {
    return this.run {
        Odgovor(getInt("PitanjeId"), getInt("odgovoreno"), id)
    }
}