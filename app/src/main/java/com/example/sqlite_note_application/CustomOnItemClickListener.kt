package com.example.sqlite_note_application

import android.view.View

class CustomOnItemClickListener(private val position : Int, private val onItemClickCallback :
 OnItemClickCallback) : View.OnClickListener {  // make CardView clickable in adapter :

    override fun onClick(view: View) {  // adjust event class OnClickListener
        onItemClickCallback.onItemClicked(view, position)
    }

    interface OnItemClickCallback {     // then implements new interface listener
        fun onItemClicked(view : View, position : Int)  // class is made to avoid final value from
    }                                                   // position that is not recommended
}