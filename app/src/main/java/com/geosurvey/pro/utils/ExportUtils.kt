package com.geosurvey.pro.utils

import android.content.Context
import android.os.Environment
import com.geosurvey.pro.domain.model.SurveyResponse
import java.io.File
import java.io.FileWriter

object ExportUtils {
    fun exportResponsesToCSV(context: Context, surveyTitle: String, responses: List<SurveyResponse>): File? {
        val fileName = "${surveyTitle.replace(" ", "_")}_responses.csv"
        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(path, fileName)

        return try {
            val writer = FileWriter(file)
            writer.append("ResponseID,RespondentID,Timestamp,Answers\n")
            responses.forEach { res ->
                writer.append("${res.id},${res.respondentId},${res.timestamp},\"${res.answers}\"\n")
            }
            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
