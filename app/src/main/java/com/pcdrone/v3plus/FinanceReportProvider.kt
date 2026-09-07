package com.pcdrone.v3plus

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File

class FinanceReportProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    private fun resolveFile(uri: Uri): File? {

        val name =
            uri.lastPathSegment
                ?.replace("/", "")
                ?.replace("\\", "")
                ?: return null

        val dir =
            File(
                context?.cacheDir,
                "finance_reports"
            )

        val file =
            File(dir, name)

        return if (
            file.exists() &&
            file.isFile
        ) {
            file
        } else {
            null
        }
    }

    override fun getType(
        uri: Uri
    ): String = "application/pdf"

    override fun openFile(
        uri: Uri,
        mode: String
    ): ParcelFileDescriptor {

        val file =
            resolveFile(uri)
                ?: throw java.io.FileNotFoundException(
                    uri.toString()
                )

        return ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {

        val file =
            resolveFile(uri)
                ?: return null

        val columns =
            projection
                ?: arrayOf(
                    OpenableColumns.DISPLAY_NAME,
                    OpenableColumns.SIZE
                )

        val cursor =
            MatrixCursor(columns)

        val row =
            cursor.newRow()

        columns.forEach { column ->

            when (column) {

                OpenableColumns.DISPLAY_NAME ->
                    row.add(file.name)

                OpenableColumns.SIZE ->
                    row.add(file.length())

                else ->
                    row.add(null)
            }
        }

        return cursor
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
