package com.example.kotlin_amateur.post

/**
 * ProfileDialogManager - 다이얼로그 스택 관리 (완성 버전)
 *
 * 🎯 기능:
 * - 최대 깊이 제한
 * - 스택 기반 뒤로가기
 * - 메모리 효율적
 * - 🆕 스택 전달/복원 (Fragment 간 이동 시)
 */
class ProfileDialogManager {
    companion object {
        const val MAX_STACK_DEPTH = 5 // 최대 5단계까지만 허용
    }

    // 🔥 기존 스택을 외부에서 접근 가능하도록 수정
    private val _dialogStack = mutableListOf<String>() // userId 스택
    val dialogStack: List<String> get() = _dialogStack.toList() // 읽기 전용 복사본

    // ===== 기존 메서드들 =====
    fun canOpenDialog(userId: String): Boolean {
        return _dialogStack.size < MAX_STACK_DEPTH && !_dialogStack.contains(userId)
    }

    fun pushDialog(userId: String): Boolean {
        if (canOpenDialog(userId)) {
            _dialogStack.add(userId)
            println("📱 [ProfileDialogManager] 프로필 추가: $userId (깊이: ${_dialogStack.size})")
            return true
        }
        println("⚠️ [ProfileDialogManager] 프로필 추가 실패: $userId (깊이: ${_dialogStack.size}, 중복: ${isInStack(userId)})")
        return false
    }

    fun popDialog(): String? {
        return if (_dialogStack.isNotEmpty()) {
            val removed = _dialogStack.removeLastOrNull()
            println("❌ [ProfileDialogManager] 프로필 제거: $removed (깊이: ${_dialogStack.size})")
            removed
        } else {
            println("⚠️ [ProfileDialogManager] 제거할 프로필 없음")
            null
        }
    }

    fun getCurrentDepth(): Int = _dialogStack.size

    fun clear() {
        val oldSize = _dialogStack.size
        _dialogStack.clear()
        println("🧹 [ProfileDialogManager] 스택 초기화 ($oldSize -> 0)")
    }

    fun isInStack(userId: String): Boolean = _dialogStack.contains(userId)

    // ===== 🆕 스택 전달/복원 기능 =====

    /**
     * 스택을 Array로 반환 (Bundle 전달용)
     */
    fun getStackAsArray(): Array<String> {
        return _dialogStack.toTypedArray()
    }

    /**
     * 저장된 스택 복원
     * @param savedStack 이전에 저장된 스택
     */
    fun restoreStack(savedStack: List<String>) {
        _dialogStack.clear()
        _dialogStack.addAll(savedStack)
        println("🔄 [ProfileDialogManager] 스택 복원 완료: ${savedStack.size}개")
        printStackState()
    }

    /**
     * 스택 상태 디버깅용 출력
     */
    fun printStackState() {
        if (_dialogStack.isEmpty()) {
            println("📊 [ProfileDialogManager] 스택: 비어있음")
        } else {
            println("📊 [ProfileDialogManager] 스택 (${_dialogStack.size}/${MAX_STACK_DEPTH}): ${_dialogStack.joinToString(" -> ")}")
        }
    }

    /**
     * 현재 최상위 프로필 ID 반환
     */
    fun getCurrentProfileId(): String? {
        return _dialogStack.lastOrNull()
    }

    /**
     * 스택에서 특정 프로필까지 모든 상위 프로필 제거
     * @param userId 유지할 프로필 ID
     */
    fun popToProfile(userId: String) {
        val index = _dialogStack.indexOf(userId)
        if (index != -1) {
            // userId 이후의 모든 항목 제거
            while (_dialogStack.size > index + 1) {
                val removed = _dialogStack.removeAt(_dialogStack.size - 1)
                println("🗑️ [ProfileDialogManager] 스택에서 제거: $removed")
            }
            println("🎯 [ProfileDialogManager] '$userId'까지 스택 정리 완료")
        } else {
            println("⚠️ [ProfileDialogManager] '$userId'가 스택에 없음")
        }
    }

    /**
     * 스택이 비어있는지 확인
     */
    fun isEmpty(): Boolean = _dialogStack.isEmpty()

    /**
     * 스택이 가득 찼는지 확인
     */
    fun isFull(): Boolean = _dialogStack.size >= MAX_STACK_DEPTH

    /**
     * 특정 깊이까지 스택 제한
     * @param maxDepth 허용할 최대 깊이
     */
    fun limitStackTo(maxDepth: Int) {
        while (_dialogStack.size > maxDepth) {
            val removed = _dialogStack.removeAt(_dialogStack.size - 1)
            println("✂️ [ProfileDialogManager] 깊이 제한으로 제거: $removed")
        }
    }

    /**
     * 스택 상태를 문자열로 직렬화
     */
    fun serializeStack(): String {
        return _dialogStack.joinToString(",")
    }

    /**
     * 문자열에서 스택 복원
     */
    fun deserializeStack(stackString: String) {
        if (stackString.isNotBlank()) {
            val stack = stackString.split(",").filter { it.isNotBlank() }
            restoreStack(stack)
        }
    }
}