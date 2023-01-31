package ba.etf.rma22.projekat.data.repositories

class ApiConfig {
    companion object {
        var baseURL = "https://rma22ws.herokuapp.com"

        fun postaviBaseURL(baseUrl:String) {
            this.baseURL = baseUrl
        }
    }
}