package ba.etf.rma22.projekat.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
data class Grupa(
    @PrimaryKey
    val id: Int,
    val naziv : String,
    val nazivIstrazivanja : String
)

fun JSONObject.parseGrupu(nazivIstrazivanja: String): Grupa {
    return this.run {
        Grupa(getInt("id"), getString("naziv"), nazivIstrazivanja)
    }
}