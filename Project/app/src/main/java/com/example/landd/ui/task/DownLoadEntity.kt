package com.example.landd.ui.task

class DownLoadEntity(
    private var DownLoadTime: String?,
    private var FileName: String?,
    private var FileType: String?,
    private var FileSize: String?,
    private var DownLoadProcess: String?,
    private var DownLoadSpeed: String?
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

    fun getDownLoadProcess(): String? {
        return DownLoadProcess
    }

    fun setDownLoadProcess(downLoadProcess: String?) {
        DownLoadProcess = downLoadProcess
    }

    fun getDownLoadSpeed(): String? {
        return DownLoadSpeed
    }

    fun setDownLoadSpeed(downLoadSpeed: String?) {
        DownLoadSpeed = downLoadSpeed
    }

    fun getDownLoadTime(): String? {
        return DownLoadTime
    }

    fun setDownLoadTime(downLoadTime: String?){
        DownLoadTime=downLoadTime
    }
}