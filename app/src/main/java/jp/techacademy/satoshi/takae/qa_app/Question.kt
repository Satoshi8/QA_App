package jp.techacademy.satoshi.takae.qa_app

import java.io.Serializable

class Question(
    val title: String,
    val body: String,
    val name: String,
    val questionUid: String,
    val genre: String,
    bytes: Int,
    val answers: ByteArray,
    answerArrayList: ArrayList<Answer>
):Serializable{
    val imageBytes:ByteArray

    init{
        imageBytes = bytes.clone()
    }
}