package ba.etf.rma22.projekat.data.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ba.etf.rma21.projekat.data.AppDatabase
import ba.etf.rma22.projekat.R
import ba.etf.rma22.projekat.data.models.Account
import ba.etf.rma22.projekat.data.models.Anketa
import ba.etf.rma22.projekat.data.models.parseAnketu
import ba.etf.rma22.projekat.data.repositories.IstrazivanjeIGrupaRepository.Companion.getGrupeZaAnketu
import ba.etf.rma22.projekat.view.FragmentAnkete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class AnketaRepository {
    companion object {
        lateinit var db: AppDatabase
        private var odgovoreneAnkete: HashMap<Anketa, Pair<Boolean, List<Int>>> = hashMapOf()

        fun napraviDatum(dan: Int, mjesec: Int, godina: Int): Date {
            return Date.from(
                LocalDate.of(godina, mjesec, dan).atStartOfDay(ZoneId.systemDefault()).toInstant()
            )
        }

        private fun getColor(anketa: Anketa): Int {
            val now = LocalDate.now()
            val atm = napraviDatum(now.dayOfMonth, now.monthValue, now.year)
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val datumPocetak = formatter.parse(anketa.datumPocetak)
            return if (datumPocetak.before(atm)) {
                if (anketa.datumRada == null) R.drawable.zelena
                else R.drawable.plava
            } else R.drawable.zuta
        }

        suspend fun getAll(): List<Anketa> {
            var offset = 1
            val ankete = arrayListOf<Anketa>()
            return withContext(Dispatchers.IO) {
                try {
                    while (true) {
                        val anketeZaOffset = getAll(offset)
                        if (anketeZaOffset.isEmpty()) break
                        ankete.addAll(anketeZaOffset)
                        offset += 1
                    }
                    ankete
                } catch (e: Exception) {
                    db.anketaDao().dajSveAnkete()
                }
            }
        }

        suspend fun getAll(offset: Int): List<Anketa> {
            return withContext(Dispatchers.IO) {
                val ankete = arrayListOf<Anketa>()
                val ruta = "${ApiConfig.baseURL}/anketa?offset=$offset"
                (URL(ruta).openConnection() as HttpURLConnection).run {
                    val jsonString = this.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONArray(jsonString)
                    for (i in 0 until json.length()) {
                        val grupe = getGrupeZaAnketu(json.getJSONObject(i).getInt("id"))
                        var grupa = ""
                        for (j in grupe.indices) {
                            grupa += if (j + 1 != grupe.size) "${grupe[j].naziv},"
                            else grupe[j].naziv
                        }
                        val istrazivanje = grupe.first().nazivIstrazivanja
                        ankete.add(json.getJSONObject(i).parseAnketu(istrazivanje, grupa))
                    }
                }
                db.anketaDao().ubaciAnkete(ankete)
                ankete
            }
        }

        suspend fun getById(id: Int): Anketa? {
            var trazenaAnketa: Anketa? = null
            return withContext(Dispatchers.IO) {
                try {
                    val ruta = "${ApiConfig.baseURL}/anketa/$id"
                    (URL(ruta).openConnection() as HttpURLConnection).run {
                        val anketaJSONString =
                            this.inputStream.bufferedReader().use { it.readText() }
                        val anketaJSON = JSONObject(anketaJSONString)
                        val grupe = getGrupeZaAnketu(anketaJSON.getInt("id"))
                        var grupa = ""
                        for (j in grupe.indices) {
                            grupa += if (j + 1 != grupe.size) "${grupe[j].naziv},"
                            else grupe[j].naziv
                        }
                        val istrazivanje = grupe.first().nazivIstrazivanja
                        trazenaAnketa = anketaJSON.parseAnketu(istrazivanje, grupa)
                        trazenaAnketa
                    }
                } catch (e: Exception) {
                    trazenaAnketa
                }
            }
        }

        suspend fun getUpisane(): List<Anketa> {
            val upisaneGrupe = IstrazivanjeIGrupaRepository.getUpisaneGrupe()
            return withContext(Dispatchers.IO) {
                try {
                    val upisaneAnkete = arrayListOf<Anketa>()
                    upisaneGrupe.forEach {
                        val ankete = getAnketeZaGrupu(it.id)
                            .filter { anketa -> anketa.nazivIstrazivanja == it.nazivIstrazivanja }
                        upisaneAnkete.addAll(ankete)
                    }
                    db.anketaDao().ubaciAnkete(upisaneAnkete)
                    upisaneAnkete
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

        suspend fun getAnketeZaGrupu(idGrupe: Int): List<Anketa> {
            return try {
                val ankete = arrayListOf<Anketa>()
                val ruta = "${ApiConfig.baseURL}/grupa/$idGrupe/ankete"
                (URL(ruta).openConnection() as HttpURLConnection).run {
                    val jsonString = this.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONArray(jsonString)
                    for (i in 0 until json.length()) {
                        val grupe = getGrupeZaAnketu(json.getJSONObject(i).getInt("id"))
                        var grupa = ""
                        for (j in grupe.indices) {
                            grupa += if (j + 1 != grupe.size) "${grupe[j].naziv},"
                            else grupe[j].naziv
                        }
                        val istrazivanje = grupe.first().nazivIstrazivanja
                        ankete.add(json.getJSONObject(i).parseAnketu(istrazivanje, grupa))
                    }
                    ankete
                }
            } catch (e: Exception) {
                val trazenaGrupa = db.grupaDao().dajGrupe().first { it.id == idGrupe }
                db.anketaDao().dajSveAnkete().filter { it.nazivGrupe.contains(trazenaGrupa.naziv) }
            }
        }

        suspend fun getMyAnkete(): List<Anketa> {
            return getUpisane().sortedBy { it.datumPocetak }
        }

        suspend fun getDone(): List<Anketa> {
            return getUpisane().filter { anketa -> getColor(anketa) == R.drawable.plava }
        }

        suspend fun getFuture(): List<Anketa> {
            return getUpisane().filter { anketa -> getColor(anketa) == R.drawable.zuta }
        }

        suspend fun getNotTaken(): List<Anketa> {
            return getUpisane().filter { anketa -> getColor(anketa) == R.drawable.crvena }
        }

        fun spasiAnketu(anketa: Anketa, zavrsena: Boolean, odgovori: List<Int>) {
            odgovoreneAnkete[anketa] = Pair(zavrsena, odgovori)
        }

        fun getOdgovore(anketa: Anketa) = odgovoreneAnkete[anketa]
    }

}

@Dao
interface AnketaDao {
    @Query("SELECT * FROM anketa")
    suspend fun dajSveAnkete(): List<Anketa>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ubaciAnkete(ankete: List<Anketa>)
}