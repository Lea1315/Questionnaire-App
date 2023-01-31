package ba.etf.rma22.projekat.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity
data class Pitanje(
    @PrimaryKey
    val id: Int,
    val naziv: String,
    val tekstPitanja: String,
    val opcije: String,
    var index: Int,
    val idAnkete: Int
)

fun JSONObject.parsePitanje(id: Int): Pitanje {
    return this.run {
        Pitanje(
            getInt("id"),
            getString("naziv"),
            getString("tekstPitanja"),
            getJSONArray("opcije").parseOpcije(),
            -1,
            id
        )
    }
}

fun JSONArray.parseOpcije(): String {
    var opcije = ""
    for (i in 0 until this.length()) {
        opcije += this.getString(i)
        if (i + 1 != this.length()) opcije += ','
    }
    return opcije
}