package com.example.smsapp.util

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteOrder
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.exp

object SpamClassifier {
    private const val TAG = "SpamClassifier"
    private const val MODEL_PATH = "kobert/tflite_kobert_cls_fp16/model.tflite"
    private const val VOCAB_PATH  = "kobert/tflite_kobert_cls_fp16/vocab.txt"

    private const val SPAM_THRESHOLD = 0.6f

    private val KEYWORDS    = listOf("무료","카톡","라인","채팅","초대","체험","당첨","대출")
    private val PHONE_REGEX = Pattern.compile("\\d{2,3}[- ]?\\d{3,4}[- ]?\\d{4}")
    private val URL_REGEX   = Pattern.compile("(https?://\\S+)|(www\\.\\S+)")

    private lateinit var interpreter: Interpreter
    private lateinit var vocab: Map<String, Int>
    private var seqLen: Int = 0

    fun init(ctx: Context) {
        interpreter = Interpreter(loadModelFile(ctx.assets, MODEL_PATH))
        seqLen      = interpreter.getInputTensor(0).shape()[1]
        vocab = ctx.assets.open(VOCAB_PATH)
            .bufferedReader()
            .useLines { lines ->
                lines.mapIndexed { idx, t -> t to idx }.toMap()
            }
        Log.d(TAG, "Initialized seqLen=$seqLen, vocabSize=${vocab.size}")
    }

    fun isSpam(text: String): Boolean {
        return try {
            // 1) 모델 실행해서 raw logit 얻어오기
            val (logitHam, logitSpam) = runModel(text)

            // 2) softmax로 스팸 확률 계산
            val e0 = exp(logitHam)
            val e1 = exp(logitSpam)
            val probSpam = (e1 / (e0 + e1)).toFloat()

            // 3) 룰 엔진 체크
            val lower = text.lowercase(Locale.getDefault())
            val ruleMatch = KEYWORDS.any { lower.contains(it) }
                    || PHONE_REGEX.matcher(text).find()
                    || URL_REGEX.matcher(text).find()
                    || text.count { it.toInt() in 0x1F300..0x1F6FF } >= 3

            // 4) 최종 결과 결정
            val result = when {
                probSpam >= SPAM_THRESHOLD            -> "SPAM (prob>=${SPAM_THRESHOLD})"
                ruleMatch                             -> "SPAM (rule engine)"
                else                                  -> "NOT SPAM"
            }
            // ← 이 한 줄로 raw→prob→결과를 다 찍는다.
            logLong(TAG, "ham=$logitHam spam=$logitSpam → probSpam=$probSpam → $result")

            result.startsWith("SPAM")
        } catch (e: Exception) {
            Log.e(TAG, "classification error", e)
            false
        }
    }

    private fun runModel(text: String): Pair<Float, Float> {
        val norm   = Normalizer.normalize(text, Normalizer.Form.NFKC)
            .lowercase(Locale.getDefault())
        val tokens = tokenize(norm)

        val cls = vocab["[CLS]"]!!.toLong()
        val sep = vocab["[SEP]"]!!.toLong()
        val unk = vocab["[UNK]"]!!.toLong()

        val vocabSize = vocab.size.toLong()
        val ids  = LongArray(seqLen) { 0L }
        val mask = LongArray(seqLen) { 0L }

        var i = 0
        ids[i] = cls.coerceIn(0L, vocabSize - 1); mask[i] = 1L; i++
        for (tok in tokens) {
            if (i >= seqLen - 1) break
            // 토큰을 vocab에서 찾고, out-of-range면 클램핑
            val rawId = vocab[tok]?.toLong() ?: unk
            val safeId = rawId.coerceIn(0L, vocabSize - 1)
            ids[i] = safeId
            mask[i] = 1L
            i++
        }
        ids[i] = sep.coerceIn(0L, vocabSize - 1)
        mask[i] = 1L

        val inputs = arrayOf(arrayOf(ids), arrayOf(mask))
        val out    = Array(1) { FloatArray(2) }

        interpreter.runForMultipleInputsOutputs(inputs, mapOf(0 to out))
        return out[0][0] to out[0][1]
    }

    private fun tokenize(text: String): List<String> {
        val regex = Regex("\\w+|[^\\w\\s]")
        val words = regex.findAll(text).map { it.value }.toList()
        val output = mutableListOf<String>()
        for (w in words) {
            if (vocab.containsKey(w)) {
                output += w
            } else {
                var start = 0
                val subs = mutableListOf<String>()
                while (start < w.length) {
                    var end = w.length
                    var cur = ""
                    while (start < end) {
                        var piece = w.substring(start, end)
                        if (start > 0) piece = "##$piece"
                        if (vocab.containsKey(piece)) {
                            cur = piece; break
                        }
                        end--
                    }
                    if (cur.isEmpty()) {
                        subs += "[UNK]"
                        break
                    }
                    subs += cur
                    start = end
                }
                output += subs
            }
        }
        return output
    }

    private fun loadModelFile(assets: AssetManager, path: String): ByteBuffer {
        val afd = assets.openFd(path)
        return FileInputStream(afd.fileDescriptor).use { fis ->
            fis.channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
                .order(ByteOrder.nativeOrder())
        }
    }

    // 로그가 너무 길면 자동으로 잘라서 찍어주는 헬퍼
    private const val MAX_LOG_LEN = 1000
    private fun logLong(tag: String, msg: String) {
        if (msg.length <= MAX_LOG_LEN) {
            Log.d(tag, msg)
        } else {
            var i = 0
            while (i < msg.length) {
                val end = minOf(msg.length, i + MAX_LOG_LEN)
                Log.d(tag, msg.substring(i, end))
                i = end
            }
        }
    }
}
