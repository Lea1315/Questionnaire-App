package ba.etf.rma22.projekat.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "anketa_taken")
data class AnketaTaken(
    @PrimaryKey
    val id: Int,
    val student: String,
    val progres: Float,
    val datumRada: String,
    val AnketumId: Int
)

fun JSONObject.parsePokusaj(): AnketaTaken {
    return this.run {
        AnketaTaken(
            getInt("id"),
            getString("student"),
            getDouble("progres").toFloat(),
            getString("datumRada"),
            getInt("AnketumId")
        )
    }
}