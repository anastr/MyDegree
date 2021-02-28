package com.github.anastr.myscore.util.drag

interface DragTouchHelper {
    fun onItemDrag(fromPosition: Int, toPosition: Int)
    fun onItemMoved()
}