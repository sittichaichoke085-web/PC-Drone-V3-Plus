package com.pcdrone.v3plus

class ReportFileProvider : android.content.ContentProvider() {

    override fun onCreate(): Boolean = true

    private fun resolveFile(
        uri: android.net.Uri
    ): java.io.File {

        val name =
            uri.lastPathSegment
                ?: throw java.io.FileNotFoundException()

        val safeName =
            java.io.File(name).name

        val folder =
            java.io.File(
                context!!.cacheDir,
                "reports"
            )

        val file =
            java.io.File(
                folder,
                safeName
            )

        if (!file.exists()) {
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

        val file =
            resolveFile(uri)

        return android.os.ParcelFileDescriptor.open(
            file,
            android.os.ParcelFileDescriptor.MODE_READ_ONLY
        )
    }

    override fun getType(
        uri: android.net.Uri
    ): String {

        val name =
            uri.lastPathSegment
                ?.lowercase()
                ?: ""

        return when {

            name.endsWith(".pdf") ->
                "application/pdf"

            name.endsWith(".csv") ->
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
            resolveFile(uri)

        val columns =
            arrayOf(
                android.provider.OpenableColumns.DISPLAY_NAME,
                android.provider.OpenableColumns.SIZE
            )

        return android.database.MatrixCursor(
            columns,
            1
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
