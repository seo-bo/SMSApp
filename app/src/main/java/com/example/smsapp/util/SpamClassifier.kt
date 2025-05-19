package com.example.smsapp.util

import android.content.Context
import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.text.Normalizer
import java.util.*
import kotlin.math.min

object SpamClassifier {
    private const val MODEL_PATH = "kobert/tflite_kobert_cls_fp16/model.tflite"
    private const val VOCAB_PATH = "kobert/tflite_kobert_cls_fp16/vocab.txt"
    private const val MAX_SEQ_LEN = 128

    private lateinit var interpreter: Interpreter
    private val vocab = HashMap<String, Int>()

    fun init(ctx: Context) {
        interpreter = Interpreter(loadModelFile(ctx.assets, MODEL_PATH))
        ctx.assets.open(VOCAB_PATH).bufferedReader().useLines { lines ->
            lines.forEachIndexed { idx, token -> vocab[token] = idx }
        }
    }

    fun isSpam(text: String): Boolean {
        val tokens = tokenize(text)
        val inputIds = IntArray(MAX_SEQ_LEN) { 0 }
        val inputMask = IntArray(MAX_SEQ_LEN) { 0 }
        val clsId = vocab["[CLS]"] ?: 101
        val sepId = vocab["[SEP]"] ?: 102
        var idx = 0
        inputIds[idx++] = clsId
        for (t in tokens) {
            if (idx >= MAX_SEQ_LEN - 1) break
            inputIds[idx] = vocab[t] ?: vocab["[UNK]"]!!
            inputMask[idx] = 1
            idx++
        }
        if (idx < MAX_SEQ_LEN) {
            inputIds[idx] = sepId
            inputMask[idx] = 1
        }
        val ib = ByteBuffer.allocateDirect(MAX_SEQ_LEN * 4).order(ByteOrder.nativeOrder())
        val mb = ByteBuffer.allocateDirect(MAX_SEQ_LEN * 4).order(ByteOrder.nativeOrder())
        for (i in 0 until MAX_SEQ_LEN) {
            ib.putInt(inputIds[i])
            mb.putInt(inputMask[i])
        }
        ib.rewind(); mb.rewind()
        val output = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder())
        output.rewind()
        interpreter.run(arrayOf(ib, mb), mapOf(0 to output))
        output.rewind()
        val scores = FloatArray(2) { output.float }
        return scores[1] > scores[0]
    }

    private fun tokenize(text: String): List<String> {
        val norm = Normalizer.normalize(text, Normalizer.Form.NFKC)
        val lowered = norm.lowercase(Locale.getDefault())
        val regex = Regex("\\w+|[^\\w\\s]")
        val words = regex.findAll(lowered).map { it.value }.toList()

        val tokens = mutableListOf<String>()
        for (word in words) {
            if (vocab.containsKey(word)) {
                tokens.add(word)
            } else {
                var start = 0
                val subTokens = mutableListOf<String>()
                var bad = false
                while (start < word.length) {
                    var end = word.length
                    var curSub = ""
                    while (start < end) {
                        var substr = word.substring(start, end)
                        if (start > 0) substr = "##$substr"
                        if (vocab.containsKey(substr)) {
                            curSub = substr
                            break
                        }
                        end--
                    }
                    if (curSub.isEmpty()) {
                        bad = true
                        break
                    }
                    subTokens.add(curSub)
                    start = end
                }
                if (bad) tokens.add("[UNK]") else tokens.addAll(subTokens)
            }
        }
        return tokens
    }

    private fun loadModelFile(assets: AssetManager, path: String): ByteBuffer {
        assets.openFd(path).let { fd ->
            FileInputStream(fd.fileDescriptor).use { fis ->
                val channel = fis.channel
                val buf = channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fd.startOffset,
                    fd.declaredLength
                )
                return buf.order(ByteOrder.nativeOrder())
            }
        }
    }
}
