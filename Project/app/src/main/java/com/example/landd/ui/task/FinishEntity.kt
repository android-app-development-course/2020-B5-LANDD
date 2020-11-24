package com.example.landd.ui.task

class FinishEntity(
    private var FileName: String?,
    private var FileType: String?,
    private var FileSize: String?,
    private var FinishTime: String?
) {


    fun getFileName(): String? {
        return FileName
    }

    fun setFileName(fileName: String?) {
        FileName = fileName
    }

    fun getFileType(): String? {
        return FileType
    }

    fun setFileType(fileType: String?) {
        FileType = fileType
    }

    fun getFileSize(): String? {
        return FileSize
    }

    fun setFileSize(fileSize: String?) {
        FileSize = fileSize
    }

    fun getFinishTime(): String? {
        return FinishTime
    }

    fun setFinishTime(finishTime: String?) {
        FinishTime = finishTime
    }
}