package ba.etf.rma22.projekat.data.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ba.etf.rma21.projekat.data.AppDatabase
import ba.etf.rma22.projekat.data.models.Odgovor
import ba.etf.rma22.projekat.data.models.parseOdgovor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class OdgovorRepository {
    companion object {
        lateinit var db: AppDatabase
        suspend fun getOdgovoriAnketa(idAnkete: Int): List<Odgovor> {
            val pokusaj = TakeAnketaRepository
                .getPoceteAnkete()?.firstOrNull { it.AnketumId == idAnkete }
                ?: return emptyList()
            return withContext(Dispatchers.IO) {
                try {
                    val odgovori = arrayListOf<Odgovor>()
                    val ruta =
                        "${ApiConfig.baseURL}/student/${AccountRepository.acHash}/anketataken/${pokusaj.id}/odgovori"
                    (URL(ruta).openConnection() as HttpURLConnection).run {
                        val odgJSONString = this.inputStream.bufferedReader().use { it.readText() }
                        val odgJSON = JSONArray(odgJSONString)
                        for (i in 0 until odgJSON.length()) {
                            odgovori.add(odgJSON.getJSONObject(i).parseOdgovor(idAnkete)!!)
                        }
                        odgovori
                    }
                } catch (e: Exception) {
                    db.odgovorDao().dajOdgovoreZaAnketu(idAnkete)
                }
            }
        }

        suspend fun postaviOdgovorAnketa(idAnketaTaken: Int, idPitanje: Int, odgovor: Int): Int {
            val pokusaj = TakeAnketaRepository.getPoceteAnkete()?.first { it.id == idAnketaTaken }!!
            val odgovori = getOdgovoriAnketa(pokusaj.AnketumId)
            val pitanja = PitanjeAnketaRepository.getPitanja(pokusaj.AnketumId)
            var progres = odgovori.size + 1f / pitanja.size
            val prvaCifra = (progres * 10).toInt()
            if (prvaCifra % 2 != 0) progres += 0.1f
            return withContext(Dispatchers.IO) {
                try {
                    val ruta = "${ApiConfig.baseURL}/student/${AccountRepository.acHash}/anketataken/$idAnketaTaken/odgovor"
                    val client = OkHttpClient()
                    val jsonBody = JSONObject()
                    jsonBody.put("odgovor", odgovor)
                        .put("pitanje", idPitanje)
                        .put("progres", ((progres * 100).toInt() / 10 * 10))

                    val request: Request = Request.Builder()
                        .url(URL(ruta))
                        .post(RequestBody.create("application/json".toMediaType(), jsonBody.toString()))
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.code == 200) {
                        db.odgovorDao().ubaciOdgovore(Odgovor(idPitanje, odgovor, pokusaj.AnketumId))
                        (progres * 100).toInt() / 10 * 10
                    }
                    else -1
                } catch (e: Exception) {
                    -1
                }
            }
        }
    }
}

@Dao
interface OdgovorDao {
    @Query("SELECT * FROM odgovor WHERE idAnkete=:id")
    suspend fun dajOdgovoreZaAnketu(id: Int): List<Odgovor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ubaciOdgovore(vararg odgovori: Odgovor)
}