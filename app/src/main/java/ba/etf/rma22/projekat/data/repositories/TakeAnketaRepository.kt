package ba.etf.rma22.projekat.data.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ba.etf.rma21.projekat.data.AppDatabase
import ba.etf.rma22.projekat.data.models.AnketaTaken
import ba.etf.rma22.projekat.data.models.parsePokusaj
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class TakeAnketaRepository {
    companion object {
        lateinit var db: AppDatabase
        suspend fun zapocniAnketu(idAnkete: Int): AnketaTaken? {
            val pokusaji = getPoceteAnkete()
            pokusaji?.let {
                val ankete = pokusaji.map { it.AnketumId }
                val stariPokusaj = ankete.indexOf(idAnkete)
                if (stariPokusaj != -1) return pokusaji[stariPokusaj]
            }
            return withContext(Dispatchers.IO) {
                var anketaTaken: AnketaTaken? = null
                val ruta =
                    "${ApiConfig.baseURL}/student/${AccountRepository.acHash}/anketa/$idAnkete"
                try {
                    (URL(ruta).openConnection() as HttpURLConnection).run {
                        requestMethod = "POST"
                        val resultString = this.inputStream.bufferedReader().use { it.readText() }
                        val resultObj = JSONObject(resultString)
                        if (!resultObj.has("message")) {
                            anketaTaken = resultObj.parsePokusaj()
                            anketaTaken?.let {
                                db.anketaTakenDao().ubaciPokusaj(it)
                            }
                        }
                    }
                    anketaTaken
                } catch (e: Exception) {
                    anketaTaken
                }
            }
        }

        suspend fun getPoceteAnkete(): List<AnketaTaken>? {
            return withContext(Dispatchers.IO) {
                val pokusaji = arrayListOf<AnketaTaken>()
                val ruta = "${ApiConfig.baseURL}/student/${AccountRepository.acHash}/anketataken"
                try {
                    (URL(ruta).openConnection() as HttpURLConnection).run {
                        val pokusajiJSONString =
                            this.inputStream.bufferedReader().use { it.readText() }
                        val pokusajiJSON = JSONArray(pokusajiJSONString)
                        for (i in 0 until pokusajiJSON.length()) {
                            pokusaji.add(pokusajiJSON.getJSONObject(i).parsePokusaj())
                        }
                        if (pokusaji.isEmpty()) null else pokusaji

                    }
                } catch (e: Exception) {
                    db.anketaTakenDao().dajSvePokusaje()
                }
            }
        }
    }
}

@Dao
interface AnketaTakenDao {
    @Query("SELECT * FROM anketa_taken")
    suspend fun dajSvePokusaje(): List<AnketaTaken>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ubaciPokusaj(pokusaj: AnketaTaken)
}