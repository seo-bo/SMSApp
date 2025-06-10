package com.example.smsapp.util

import android.content.Context
import com.example.smsapp.data.KeywordEntity
import com.example.smsapp.data.SmsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.ArrayDeque
import java.util.HashMap

object KeywordMatcher {

    private class Node {
        val next: MutableMap<Char, Node> = HashMap()
        var fail: Node? = null
        var isEnd = false
    }

    // White Trie / Black Trie
    private object White { val root = Node() }
    private object Black { val root = Node() }

    @Volatile private var built = false

    suspend fun init(ctx: Context) = withContext(Dispatchers.IO) {
        if (built) return@withContext

        // 읽기
        val list = SmsDatabase.get(ctx).keywordDao().getAll()

        synchronized(this@KeywordMatcher) {
            if (built) return@synchronized

            // 트라이 실패 링크 초기화
            White.root.next.clear(); White.root.fail = null
            Black.root.next.clear(); Black.root.fail = null

            // 삽입
            list.forEach { e -> insert(e.word.lowercase(Locale.getDefault()), e.isWhitelist) }

            // 실패 링크 빌드
            buildAC(White.root)
            buildAC(Black.root)

            built = true
        }
    }

    fun invalidate() = synchronized(this) {
        built = false
    }

    // 매칭?
    fun match(text: String): Result {
        if (!built) throw IllegalStateException("KeywordMatcher.init() 먼저 호출해야 합니다")

        val lower = text.lowercase(Locale.getDefault())
        val whiteHit = search(lower, White.root)
        val blackHit = if (whiteHit) false else search(lower, Black.root) // 화이트 이기면 블랙 무시

        return Result(whiteHit, blackHit)
    }

    data class Result(val hasWhite: Boolean, val hasBlack: Boolean)

    private fun insert(word: String, isWhite: Boolean) {
        var node = if (isWhite) White.root else Black.root
        for (ch in word) node = node.next.getOrPut(ch) { Node() }
        node.isEnd = true
    }

    // bfs로 fail link 구성
    private fun buildAC(root: Node) {
        val q: ArrayDeque<Node> = ArrayDeque()
        root.fail = root
        root.next.values.forEach { child ->
            child.fail = root
            q.add(child)
        }
        while (q.isNotEmpty()) {
            val cur = q.removeFirst()
            for ((c, nxt) in cur.next) {
                var f = cur.fail
                while (f !== root && c !in f!!.next) f = f.fail
                nxt.fail = if (c in f!!.next) f.next[c]!! else root
                nxt.isEnd = nxt.isEnd || nxt.fail!!.isEnd
                q.add(nxt)
            }
        }
    }

    // hit?
    private fun search(text: String, root: Node): Boolean {
        var cur = root
        for (c in text) {
            while (cur !== root && c !in cur.next) cur = cur.fail!!
            if (c in cur.next) cur = cur.next[c]!!
            if (cur.isEnd) return true
        }
        return false
    }
}
