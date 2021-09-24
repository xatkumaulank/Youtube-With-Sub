package com.example.playyoutubevideo

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.widget.TextView
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.downloader.YoutubeCallback
import com.github.kiulian.downloader.downloader.request.RequestSubtitlesDownload
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo
import com.github.kiulian.downloader.downloader.response.Response
import com.github.kiulian.downloader.model.Extension
import com.github.kiulian.downloader.model.subtitles.SubtitlesInfo
import com.github.kiulian.downloader.model.videos.VideoInfo
import org.json.JSONObject


open class ExtractSubtitleTask(private var context: Context?, private var textView: TextView?,
                               var myMap : MutableMap<String,String>
)
    : AsyncTask<String, Void, String>() {
    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String?): String? {

         try {
             val builder = StringBuilder()
             val downloader = YoutubeDownloader()
             val request : RequestVideoInfo = RequestVideoInfo(params[0])
                     .callback(object : YoutubeCallback<VideoInfo> {
                         override fun onFinished(data: VideoInfo?) {

                         }

                         override fun onError(throwable: Throwable?) {

                         }

                     }).async()
             val response: Response<VideoInfo> = downloader.getVideoInfo(request)
             val video = response.data()




            val subtitlesInfo: List<SubtitlesInfo> = video.subtitlesInfo() // NOTE: includes auto-generated

            for (info in subtitlesInfo) {
                val myRequest = RequestSubtitlesDownload(info)
                        .formatTo(Extension.JSON3)
                        .async()
                val myResponse = downloader.downloadSubtitle(myRequest)

                val subtitlesString = myResponse.data()

                val jsonObject = JSONObject(subtitlesString)
                val jsonArray  = jsonObject.getJSONArray("events")

                for(i in 0 until jsonArray.length()){
                    if (jsonArray.getJSONObject(i).has("segs")){
                        val tStartMs = jsonArray.getJSONObject(i).getInt("tStartMs")
                        builder.append("[${tStartMs}s]")
                        val segs = jsonArray.getJSONObject(i).getJSONArray("segs")
                        for (j in 0 until segs.length()){
                            val utf8 = segs.getJSONObject(j).getString("utf8")
                            builder.append(utf8)
                        }
                        builder.append("[end]")
                    }
                }
            }
            return builder.toString()
        }catch (ex: Exception){
            ex.printStackTrace()
            return "Đã có lỗi xảy ra"
        }
    }
    private fun convertSubToMap(subText : String?) : MutableMap<String,String>{
        val subTextArray = subText?.split("[end]")
        val myMap = mutableMapOf<String,String>()

        if (subTextArray != null) {
            for (i in subTextArray.indices){
                val nextSplit = subTextArray[i].split("s]")
                for (j in nextSplit.indices){
                    if (nextSplit[j] != ""){
                        val key = nextSplit[0].removePrefix("[")
                        val value = nextSplit[1]
                        myMap[key] = value
                    }
                }
            }
        }
        return myMap
    }

     override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        textView?.text = result
        myMap = convertSubToMap(result)
    }

}