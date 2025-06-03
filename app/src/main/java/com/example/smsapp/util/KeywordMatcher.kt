package com.example.smsapp.util

import android.content.Context
import com.example.smsapp.data.KeywordEntity
import com.example.smsapp.data.SmsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.ArrayDeque
import java.util.HashMap

/**
 *  ┌────────────────────────────────────────┐
 *  │   화이트 전용 Trie  |   블랙 전용 Trie  │  ← 두 개를 따로 둠
 *  └────────────────────────────────────────┘
 *
 *  • 매칭 순서
 *      1) 화이트 키워드가 1개라도 hit  →  무조건 통과
 *      2) 블랙 키워드 hit              →  차단
 *      3) 둘 다 miss                   →  SpamClassifier 에게 위임
 *
 *  • init(ctx)  : suspend, 언제든 호출 가능 (중복 호출 시 0-cost)
 *  • invalidate(): 키워드 CRUD 시 호출 → 다음 init 때 재빌드
 *  • match(text) : 화이트/블랙 hit 여부 반환
 */
object KeywordMatcher {

    /* ────── 노드 정의 ────── */
    private class Node {
        val next: MutableMap<Char, Node> = HashMap()
        var fail: Node? = null
        var isEnd = false               // 끝 토큰 여부
    }

    /* ────── 두 개의 Trie ────── */
    private object White { val root = Node() }
    private object Black { val root = Node() }

    @Volatile private var built = false

    /* ─────────────────────────────────────────
       1) init(ctx)      – suspend / 중복호출 허용
       2) invalidate()   – 키워드 변경 시 호출
    ───────────────────────────────────────── */

    suspend fun init(ctx: Context) = withContext(Dispatchers.IO) {
        if (built) return@withContext            // fast-path

        /* ① DB 목록 읽기 (suspend 지점) */
        val list = SmsDatabase.get(ctx).keywordDao().getAll()

        /* ② 동기화 범위를 최소화하여 빌드 */
        synchronized(this@KeywordMatcher) {
            if (built) return@synchronized       // double-check

            // 기존 트리 초기화
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

    /* ────── 매칭 함수 ────── */
    fun match(text: String): Result {
        if (!built) throw IllegalStateException("KeywordMatcher.init() 먼저 호출해야 합니다")

        val lower = text.lowercase(Locale.getDefault())
        val whiteHit = search(lower, White.root)
        val blackHit = if (whiteHit) false else search(lower, Black.root) // 화이트 이기면 블랙 무시

        return Result(whiteHit, blackHit)
    }

    data class Result(val hasWhite: Boolean, val hasBlack: Boolean)

    /* ────── 내부 헬퍼 ────── */

    private fun insert(word: String, isWhite: Boolean) {
        var node = if (isWhite) White.root else Black.root
        for (ch in word) node = node.next.getOrPut(ch) { Node() }
        node.isEnd = true
    }

    /** Aho–Corasick 실패 링크 구성 */
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
                nxt.isEnd = nxt.isEnd || nxt.fail!!.isEnd       // 출력 병합
                q.add(nxt)
            }
        }
    }

    /** 텍스트 1회 검색 → hit 여부 */
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
