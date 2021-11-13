package com.assignment.photoeditor.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class EditorViewModels: ViewModel() {

    private var stackOfBitmap:MutableLiveData<List<Bitmap>> =MutableLiveData<List<Bitmap>>()
    private var bitmapStack: MutableList<Bitmap> = mutableListOf()
    val getBitmapFromStack:LiveData<Bitmap> = Transformations.switchMap(stackOfBitmap,::getImage)

    private fun getImage(list: List<Bitmap>): LiveData<Bitmap> {
        return MutableLiveData(list[list.size - 1])
    }


    public fun addBitmap(bitmap: Bitmap){
        bitmapStack.add(bitmap)
        stackOfBitmap.value=bitmapStack
    }

    public fun undo():Int{
        if (bitmapStack.size!=1){
            bitmapStack.removeAt(bitmapStack.size-1)
            stackOfBitmap.value=bitmapStack
        }
        return bitmapStack.size
    }

    public fun clearStack(){
        bitmapStack.clear()
        stackOfBitmap.value=bitmapStack
    }


}