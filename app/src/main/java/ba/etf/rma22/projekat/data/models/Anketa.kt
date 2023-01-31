package ba.etf.rma22.projekat.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
data class Anketa(
    @PrimaryKey
    val id: Int,
    val naziv: String,
    var nazivIstrazivanja: String?,
    val datumPocetak: String,
    val datumKraj: String,
    val datumRada: String?,
    val trajanje: Int,
    var nazivGrupe: String,
    val progres: Float?
)


fun JSONObject.parseAnketu(istrazivanje: String, grupa: String): Anketa {
    return this.run {
        Anketa(
            getInt("id"),
            getString("naziv"),
            istrazivanje,
            getString("datumPocetak"),
            get("datumKraj").toString(),
            null,
            getInt("trajanje"),
            grupa,
            null
        )
    }
}
