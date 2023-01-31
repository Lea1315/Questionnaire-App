package ba.etf.rma22.projekat.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
data class Istrazivanje (
    @PrimaryKey
    val id: Int,
    val naziv : String,
    val godina : Int //od 1 do 5
    )

fun JSONObject.parseIstrazivanje() : Istrazivanje {
    return this.run{
        Istrazivanje(getInt("id"), getString("naziv"), getInt("godina"))
    }
}