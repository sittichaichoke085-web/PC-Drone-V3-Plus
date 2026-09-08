package com.pcdrone.v3plus

class FinanceReportProvider :
    android.content.ContentProvider() {

    override fun onCreate():
        Boolean = true

    private fun fileFor(
        uri: android.net.Uri
    ): java.io.File {

        val raw =
            uri.lastPathSegment
                ?: throw java.io.FileNotFoundException()

        val safe =
            java.io.File(raw).name

        val dir =
            java.io.File(
                context!!.cacheDir,
                "finance_reports"
            )

        val file =
            java.io.File(
                dir,
                safe
            )

        if (
            !file.exists() ||
            !file.isFile
        ) {
            throw java.io.FileNotFoundException(
                file.absolutePath
            )
        }

        return file
    }

    override fun openFile(
        uri: android.net.Uri,
        mode: String
    ): android.os.ParcelFileDescriptor {

        return android.os.ParcelFileDescriptor.open(
            fileFor(uri),
            android.os.ParcelFileDescriptor.MODE_READ_ONLY
        )
    }

    override fun getType(
        uri: android.net.Uri
    ): String {

        val n =
            uri.lastPathSegment
                ?.lowercase()
                ?: ""

        return when {

            n.endsWith(".pdf") ->
                "application/pdf"

            n.endsWith(".csv") ->
                "text/csv"

            else ->
                "application/octet-stream"
        }
    }

    override fun query(
        uri: android.net.Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): android.database.Cursor {

        val file =
            fileFor(uri)

        return android.database.MatrixCursor(
            arrayOf(
                android.provider.OpenableColumns.DISPLAY_NAME,
                android.provider.OpenableColumns.SIZE
            )
        ).apply {

            addRow(
                arrayOf(
                    file.name,
                    file.length()
                )
            )
        }
    }

    override fun insert(
        uri: android.net.Uri,
        values: android.content.ContentValues?
    ): android.net.Uri? = null

    override fun delete(
        uri: android.net.Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun update(
        uri: android.net.Uri,
        values: android.content.ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
