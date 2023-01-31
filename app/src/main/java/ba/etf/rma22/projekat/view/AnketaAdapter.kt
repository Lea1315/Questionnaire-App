package ba.etf.rma22.projekat.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ba.etf.rma22.projekat.R
import ba.etf.rma22.projekat.data.models.Anketa
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class AnketaAdapter(
    var anketeList: List<Anketa>,
    var context: Context,
    private var onItemClick: (anketa : Anketa) -> Unit) : RecyclerView.Adapter<AnketaAdapter.AnketaViewHolder>() {

    inner class AnketaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView : CircleImageView = itemView.findViewById(R.id.imageCircle)
        var textAnketa : TextView = itemView.findViewById(R.id.nazivAnkete)
        var textIstrazivanja : TextView = itemView.findViewById(R.id.nazivIstrazivanja)
        var progresBar : ProgressBar = itemView.findViewById(R.id.progresZavrsetka)
        var textDatuma : TextView = itemView.findViewById(R.id.textDatuma)

        fun bindAnketa(anketa : Anketa, context: Context) {
            textAnketa.text = anketa.naziv
            textIstrazivanja.text = anketa.nazivIstrazivanja
            //anketa.progres?.let { progresBar.progress = it.toInt() }
            //imageView.setImageResource(odrediKrug(anketa))
            //textDatuma.text = odrediTekstDatuma(anketa)
            itemView.setOnClickListener {
                onItemClick.invoke(anketa)
            }
        }
    }

    /*fun odrediKrug(anketa : Anketa) : Int {
        val danasnjiDatum : Date = Calendar.getInstance().time

        if(danasnjiDatum.before(anketa.datumPocetak)) return R.drawable.zuta
        else if(anketa.datumRada != null && (anketa.datumPocetak.before(anketa.datumRada) || (anketa.datumPocetak).equals(anketa.datumRada)) &&
            (anketa.datumKraj.after(anketa.datumRada) || (anketa.datumKraj).equals(anketa.datumRada))) return R.drawable.plava
        else if((anketa.datumKraj).before(danasnjiDatum) && anketa.datumRada == null) return R.drawable.crvena

        return R.drawable.zelena
    }*/

    /*fun odrediTekstDatuma(anketa : Anketa) : String {
        if(odrediKrug(anketa) == R.drawable.plava) return "Anketa urađena: " + getDatum(anketa)
        else if(odrediKrug(anketa) == R.drawable.crvena) return "Anketa zatvorena: " + getDatum(anketa)
        else if(odrediKrug(anketa) == R.drawable.zuta) return "Vrijeme aktiviranja: " + getDatum(anketa)
        return "Vrijeme zatvaranja: " + getDatum(anketa)
    }*/

    fun odrediProgres(anketa : Anketa) : Int {
        val progres : Float = anketa.progres!!
        if(progres < 0.1) return 0
        else if(progres >= 0.1 && progres < 0.3) return 20
        else if(progres >= 0.3 && progres < 0.5) return 40
        else if(progres >= 0.5 && progres < 0.7) return 60
        else if(progres >= 0.7 && progres < 0.9) return 80
        return 100
    }

    /*fun getDatum(anketa : Anketa) : String {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = when(odrediKrug(anketa)) {
            R.drawable.plava -> anketa.datumRada
            R.drawable.zuta -> anketa.datumPocetak
            else -> anketa.datumKraj
        }
        return formatter.format(date!!)
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnketaViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.anketa, parent, false)
        return AnketaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnketaViewHolder, position: Int) {
        holder.bindAnketa(anketeList[position], this.context)
        //holder.itemView.setOnClickListener { onItemClick(anketeList[position]) }
    }

    override fun getItemCount(): Int = anketeList.size

    fun updateAnkete(ankete : List<Anketa>) {
        anketeList = ankete
        notifyDataSetChanged()
    }
}