package ba.etf.rma22.projekat.data.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ba.etf.rma21.projekat.data.AppDatabase
import ba.etf.rma22.projekat.data.models.Pitanje
import ba.etf.rma22.projekat.data.models.PitanjeAnketa
import ba.etf.rma22.projekat.data.models.parsePitanje
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class PitanjeAnketaRepository {
    companion object {
        lateinit var db: AppDatabase

        suspend fun getPitanja(idAnkete: Int) : List<Pitanje> {
            return withContext(Dispatchers.IO) {
                try {
                    val pitanja = arrayListOf<Pitanje>()
                    val ruta = "${ApiConfig.baseURL}/anketa/$idAnkete/pitanja"
                    (URL(ruta).openConnection() as HttpURLConnection).run {
                        val jsonString = this.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONArray(jsonString)
                        for (i in 0 until json.length()) {
                            pitanja.add(json.getJSONObject(i).parsePitanje(idAnkete))
                        }
                        disconnect()
                        db.pitanjeDao().ubaciPitanja(pitanja)
                        pitanja
                    }
                }catch (e: Exception) {
                    db.pitanjeDao().dajPitanjaZaAnketu(idAnkete)
                }
            }
        }
    }
}

@Dao
interface PitanjeDao {
    @Query("SELECT * FROM pitanje WHERE idAnkete=:id")
    suspend fun dajPitanjaZaAnketu(id: Int): List<Pitanje>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ubaciPitanja(pitanja: List<Pitanje>)
}