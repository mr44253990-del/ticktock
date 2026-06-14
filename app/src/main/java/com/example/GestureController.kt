package com.example

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class GestureAction {
    SCROLL_DOWN,
    SCROLL_UP,
    LIKE,
    LIKE_AND_REACTION,
    PAUSE_RESUME,
    EMERGENCY_STOP
}

object GestureController {
    private val _gestureFlow = MutableSharedFlow<GestureAction>(extraBufferCapacity = 1)
    val gestureFlow = _gestureFlow.asSharedFlow()

    fun emitGesture(action: GestureAction) {
        _gestureFlow.tryEmit(action)
    }
}
