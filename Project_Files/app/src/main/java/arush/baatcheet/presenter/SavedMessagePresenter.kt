package arush.baatcheet.presenter

import android.content.Context
import arush.baatcheet.model.FileHandler
import arush.baatcheet.model.SaveMessageModel

class SavedMessagePresenter (private val context: Context){
    fun getSaveMessage(): ArrayList<SaveMessageModel>{
        return FileHandler(context).retrieveSavedMessage()
    }
}